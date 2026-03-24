package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PriceProducer es responsable de scrapear productos de HardGamers.com.ar
 * basándose en las keywords de las suscripciones activas y enviar eventos de precio a Kafka.
 * HardGamers es un buscador de precios de hardware de tiendas argentinas que renderiza server-side.
 */
@Service
@Slf4j
public class PriceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private static final String TOPIC = "prices-topic";
    private static final String BASE_URL = "https://www.hardgamers.com.ar";

    @Value("${hardgamers.results-limit:5}")
    private int resultsLimit;

    public PriceProducer(KafkaTemplate<String, Object> kafkaTemplate,
                         SubscriptionRepository subscriptionRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Método programado que se ejecuta periódicamente.
     * Obtiene las keywords únicas de las suscripciones activas y busca productos
     * en HardGamers para cada una, enviando los eventos de precio a Kafka.
     */
    @Scheduled(fixedRateString = "${hardgamers.scan-interval:60000}")
    public void scanMarket() {
        log.info("Iniciando escaneo de mercado en HardGamers...");

        Set<String> keywords = getActiveKeywords();

        if (keywords.isEmpty()) {
            log.info("No hay suscripciones activas. Saltando escaneo.");
            return;
        }

        for (String keyword : keywords) {
            try {
                List<PriceEvent> events = scrapeProducts(keyword);
                for (PriceEvent event : events) {
                    log.info("Precio detectado para {}: ${}", event.getProductName(),
                            String.format("%.2f", event.getPrice()));
                    kafkaTemplate.send(TOPIC, event.getProductId(), event);
                }
            } catch (Exception e) {
                log.error("Error al escanear keyword '{}': {}", keyword, e.getMessage());
            }
        }
    }

    /**
     * Obtiene las keywords únicas de todas las suscripciones activas.
     */
    Set<String> getActiveKeywords() {
        return subscriptionRepository.findAll().stream()
                .map(Subscription::getKeyword)
                .collect(Collectors.toSet());
    }

    /**
     * Scrapea productos de HardGamers para una keyword específica.
     * Parsea el HTML de la página de resultados y extrae nombre, precio, link e imagen.
     *
     * @param keyword Término de búsqueda (ej: "notebook", "placa de video", "ram 16gb")
     * @return Lista de PriceEvent con los productos encontrados
     * @throws IOException Si hay un error al conectar con HardGamers
     */
    List<PriceEvent> scrapeProducts(String keyword) throws IOException {
        String url = BASE_URL + "/search?text=" + keyword;
        log.info("Scrapeando HardGamers: '{}'", keyword);

        Document doc = fetchDocument(url);
        Elements productCards = doc.select("article.product, article[itemtype='http://schema.org/Product']");

        List<PriceEvent> events = new ArrayList<>();

        int count = 0;
        for (Element card : productCards) {
            if (count >= resultsLimit) break;

            try {
                PriceEvent event = parseProductCard(card);
                if (event != null && event.getPrice() > 0) {
                    events.add(event);
                    count++;
                }
            } catch (Exception e) {
                log.warn("Error al parsear producto: {}", e.getMessage());
            }
        }

        log.info("Encontrados {} productos para keyword: '{}'", events.size(), keyword);
        return events;
    }

    /**
     * Obtiene el Document HTML desde una URL. Extraído a método separado para facilitar testing.
     */
    Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
    }

    /**
     * Parsea un elemento article de producto de HardGamers y extrae los datos relevantes.
     *
     * Estructura HTML esperada:
     * <article class="product">
     *   <img itemprop="image" src="..." alt="...">
     *   <a itemprop="url" href="/product/ID"><h3 itemprop="name">TITULO</h3></a>
     *   <h4 class="subtitle">TIENDA</h4>
     *   <h2 itemprop="price" content="3990">3.990</h2>
     * </article>
     */
    PriceEvent parseProductCard(Element card) {
        // Extraer título
        Element titleEl = card.selectFirst("h3[itemprop=name]");
        if (titleEl == null) titleEl = card.selectFirst("h3.product-title");
        String title = titleEl != null ? titleEl.text().trim() : null;

        if (title == null || title.isEmpty()) return null;

        // Extraer precio del atributo content (valor numérico limpio)
        Element priceEl = card.selectFirst("[itemprop=price]");
        double price = 0.0;
        if (priceEl != null) {
            String priceContent = priceEl.attr("content");
            if (!priceContent.isEmpty()) {
                price = Double.parseDouble(priceContent);
            } else {
                // Fallback: parsear el texto visible
                String priceText = priceEl.text().replaceAll("[^\\d,.]", "").replace(".", "").replace(",", ".");
                if (!priceText.isEmpty()) {
                    price = Double.parseDouble(priceText);
                }
            }
        }

        // Extraer link
        Element linkEl = card.selectFirst("a[itemprop=url]");
        String permalink = "";
        String productId = "";
        if (linkEl != null) {
            String href = linkEl.attr("href");
            permalink = href.startsWith("http") ? href : BASE_URL + href;
            // Extraer ID del producto del path /product/ID
            String[] parts = href.split("/product/");
            if (parts.length > 1) {
                productId = parts[1];
            }
        }

        if (productId.isEmpty()) {
            // Fallback: generar un ID basado en el div de favoritos
            Element favEl = card.selectFirst(".favourite");
            productId = favEl != null ? favEl.id() : String.valueOf(title.hashCode());
        }

        // Extraer imagen
        Element imgEl = card.selectFirst("img[itemprop=image]");
        String thumbnail = imgEl != null ? imgEl.attr("src") : "";

        return new PriceEvent(productId, title, price, permalink, thumbnail, System.currentTimeMillis());
    }
}