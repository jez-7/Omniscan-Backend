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
 * PriceProducer is responsible for scraping products from HardGamers.com.ar
 * based on the keywords from active subscriptions and sending price events to Kafka.
 * HardGamers is a hardware price aggregator from Argentine stores that renders server-side.
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
     * Scheduled method that runs periodically.
     * Retrieves unique keywords from active subscriptions and searches for products
     * on HardGamers for each one, sending price events to Kafka.
     */
    @Scheduled(fixedRateString = "${hardgamers.scan-interval:60000}")
    public void scanMarket() {
        log.info("Starting market scan on HardGamers...");

        Set<String> keywords = getActiveKeywords();

        if (keywords.isEmpty()) {
            log.info("No active subscriptions. Skipping scan.");
            return;
        }

        for (String keyword : keywords) {
            try {
                List<PriceEvent> events = scrapeProducts(keyword);
                for (PriceEvent event : events) {
                    log.info("Price detected for {}: ${}", event.getProductName(),
                            String.format("%.2f", event.getPrice()));
                    kafkaTemplate.send(TOPIC, event.getProductId(), event);
                    Thread.sleep(2000);
                }
            } catch(InterruptedException ie){
                log.error("Scan for '{}' was interrupted: {}", keyword, ie.getMessage());
                Thread.currentThread().interrupt();
            } catch(Exception e){
                log.error("Error scanning keyword '{}': {}", keyword, e.getMessage());
            }
        }

    }

    /**
     * Retrieves unique keywords from all active subscriptions.
     */
    Set<String> getActiveKeywords() {
        return subscriptionRepository.findAll().stream()
                .map(Subscription::getKeyword)
                .collect(Collectors.toSet());
    }

    /**
     * Scrapes products from HardGamers for a specific keyword.
     * Parses the HTML from the results page and extracts name, price, link, and image.
     *
     * @param keyword Search term
     * @return List of PriceEvent with the found products
     * @throws IOException if there is an error connecting to HardGamers
     */
    List<PriceEvent> scrapeProducts(String keyword) throws IOException {
        String encodedKeyword = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        String url = BASE_URL + "/search?text=" + encodedKeyword;
        log.info("Scraping HardGamers for: '{}'", keyword);

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
                log.warn("Error parsing product: {}", e.getMessage());
            }
        }

        log.info("Found {} products for keyword: '{}'", events.size(), keyword);
        return events;
    }

    /**
     * Fetches the HTML Document from a URL. Extracted into a separate method to facilitate testing.
     */
    Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();
    }

    /**
     * Parses a HardGamers product article element and extracts the relevant data.
     *
     * Expected HTML structure:
     * <article class="product">
     *   <img itemprop="image" src="..." alt="...">
     *   <a itemprop="url" href="/product/ID"><h3 itemprop="name">TITLE</h3></a>
     *   <h4 class="subtitle">STORE</h4>
     *   <h2 itemprop="price" content="3990">3.990</h2>
     * </article>
     */
    PriceEvent parseProductCard(Element card) {
        // Extract title
        Element titleEl = card.selectFirst("h3[itemprop=name]");
        if (titleEl == null) titleEl = card.selectFirst("h3.product-title");
        String title = titleEl != null ? titleEl.text().trim() : null;

        if (title == null || title.isEmpty()) return null;

        // Extract price from the content attribute (clean numeric value)
        Element priceEl = card.selectFirst("[itemprop=price]");
        double price = 0.0;
        if (priceEl != null) {
            String priceContent = priceEl.attr("content");
            if (!priceContent.isEmpty()) {
                price = Double.parseDouble(priceContent);
            } else {
                // Fallback: parse the visible text
                String priceText = priceEl.text().replaceAll("[^\\d,.]", "").replace(".", "").replace(",", ".");
                if (!priceText.isEmpty()) {
                    price = Double.parseDouble(priceText);
                }
            }
        }

        // Extract link
        Element linkEl = card.selectFirst("a[itemprop=url]");
        String permalink = "";
        String productId = "";
        if (linkEl != null) {
            String href = linkEl.attr("href");
            permalink = href.startsWith("http") ? href : BASE_URL + href;
            // Extract product ID from the path /product/ID
            String[] parts = href.split("/product/");
            if (parts.length > 1) {
                productId = parts[1];
            }
        }

        if (productId.isEmpty()) {
            // Fallback: generate an ID based on the favourites div
            Element favEl = card.selectFirst(".favourite");
            productId = favEl != null ? favEl.id() : String.valueOf(title.hashCode());
        }

        // Extract image
        Element imgEl = card.selectFirst("img[itemprop=image]");
        String thumbnail = imgEl != null ? imgEl.attr("src") : "";

        return new PriceEvent(productId, title, price, permalink, thumbnail, System.currentTimeMillis());
    }
}