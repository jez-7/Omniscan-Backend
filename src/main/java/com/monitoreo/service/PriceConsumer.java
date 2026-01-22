package com.monitoreo.service;

import com.monitoreo.model.PriceEvent;
import com.monitoreo.model.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PriceConsumer {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PriceRepository priceRepository;

    @KafkaListener(topics = "prices-topic", groupId = "price-monitor-group")
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

            log.info("📊 Producto: {} | Actual: ${} | Promedio Ventana: ${}",
                    event.getProductName(), event.getPrice(), String.format("%.2f", average));

            // si el precio es un 5% menor al promedio se guarda la oferta
            if (event.getPrice() < (average * 0.95)) {
                log.info("🎯 ¡OFERTA POR VOLATILIDAD DETECTADA!");

                PriceHistory history = new PriceHistory();
                history.setProductId(event.getProductId());
                history.setPrice(event.getPrice());
                history.setTimestamp(new Date());

                priceRepository.save(history); // se persiste en db
            }
        }
    }

}
