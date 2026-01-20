package com.monitoreo.service;

import com.monitoreo.model.PriceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriceProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "prices-topic";

    public PriceProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // simula escaneo cada 5 segundos
    @Scheduled(fixedRate = 5000)
    public void simulatePriceScan() {
        PriceEvent event = new PriceEvent(
                "prod-101",
                "Monitor Gamer 24'",
                Math.random() * (500 - 400) + 400, // precio random entre 400 y 500
                System.currentTimeMillis()
        );

        log.info("📢 Produciendo evento de precio: {}", event);
        kafkaTemplate.send(TOPIC, event.getProductId(), event);
    }
}
