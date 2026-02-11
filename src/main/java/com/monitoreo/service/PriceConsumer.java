package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceConsumer {

    private final StringRedisTemplate redisTemplate;

    private final PriceRepository priceRepository;

    private final NotificationService notificationService;

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

                PriceHistory history = new PriceHistory();
                history.setProductId(event.getProductId());
                history.setProductName(event.getProductName());
                history.setPrice(event.getPrice());
                history.setPermalink(event.getPermalink());
                history.setThumbnail(event.getThumbnail());
                history.setTimestamp(new Date());

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
