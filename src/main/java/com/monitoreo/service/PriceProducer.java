package com.monitoreo.service;

import com.monitoreo.model.PriceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PriceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String TOPIC = "prices-topic";

    public PriceProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 6000)
    public void fetchAndSimulateVolatility() {
        log.info("🌐 Escaneando mercado...");

        try {

            String url = "https://dummyjson.com/products/category/laptops";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> products = (List<Map<String, Object>>) response.get("products");

            if (products != null) {
                products.stream().limit(5).forEach(item -> {
                    double basePrice = Double.parseDouble(item.get("price").toString());

                    // se simula un movimiento en los precios porque los precios de la api son estaticos, se le aplica un 10% de variacion
                    //  permite que el promedio en redis se mueva y genere ofertas reales
                    double simulatedPrice = basePrice * (0.90 + (Math.random() * 0.20));

                    PriceEvent event = new PriceEvent(
                            item.get("id").toString(),
                            item.get("title").toString(),
                            simulatedPrice,
                            "https://dummyjson.com/products/" + item.get("id"),
                            item.get("thumbnail").toString(),
                            System.currentTimeMillis()
                    );

                    log.info("📢 Precio detectado para {}: ${}",
                            event.getProductName(), String.format("%.2f", event.getPrice()));

                    kafkaTemplate.send(TOPIC, event.getProductId(), event);
                });
            }
        } catch (Exception e) {
            log.error("Error en el escaneo: {}", e.getMessage());
        }
    }
}