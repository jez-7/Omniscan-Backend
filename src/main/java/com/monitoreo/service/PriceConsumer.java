package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Servicio encargado de procesar los eventos de precios recibidos desde Kafka.
 * Realiza el cálculo de promedios móviles usando Redis y determina si existe una oferta.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PriceConsumer {

    private final StringRedisTemplate redisTemplate;

    private final PriceRepository priceRepository;

    private final NotificationService notificationService;

    /**
     * Método que consume los eventos de precios desde el tópico de Kafka.
     * Almacena los precios en Redis para calcular el promedio móvil y detecta ofertas.
     *
     * @param event El evento de precio recibido desde Kafka.
     */
    @KafkaListener(topics = "prices-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePriceEvent(PriceEvent event) {

        String redisKey = "product:window:" + event.getProductId();

        redisTemplate.opsForList().leftPush(redisKey, event.getPrice().toString());
        redisTemplate.opsForList().trim(redisKey, 0, 9); // mantener solo los últimos 10 precios

        // se obtiene todos los precios de la ventana y se calcula promedio
        List<String> prices = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (prices != null && !prices.isEmpty()) {

            double average = prices.stream()
                    .mapToDouble(Double::parseDouble)
                    .average()
                    .orElse(0.0);

            log.info("Item: {} | Actual: ${} | Promedio: ${}",
                    event.getProductName(), event.getPrice(), String.format("%.2f", average));

            // si el precio es un 5% menor al promedio se guarda en db
            if (event.getPrice() < (average * 0.95)) {
                log.info(" ---- ¡OFERTA DETECTADA! ----\"");

                PriceHistory history = PriceHistory.builder()
                        .productId(event.getProductId())
                        .productName(event.getProductName())
                        .price(event.getPrice())
                        .timestamp(new Date(event.getTimestamp()))
                        .permalink(event.getPermalink())
                        .thumbnail(event.getThumbnail())
                        .build();

                priceRepository.save(history);

                notificationService.sendTelegramAlert(
                        event.getProductName(),
                        event.getPrice(),
                        event.getPermalink()
                );
            }
        }
    }

}
