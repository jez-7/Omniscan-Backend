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

@Service
@Slf4j
public class PriceConsumer {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PriceRepository priceRepository;

    @KafkaListener(topics = "prices-topic", groupId = "price-monitor-group")
    public void consumePriceEvent(PriceEvent event) {

        String redisKey = "product:price:" + event.getProductId();

        // precio anterior de Redis
        String lastPriceStr = redisTemplate.opsForValue().get(redisKey);
        Double lastPrice = (lastPriceStr != null) ? Double.valueOf(lastPriceStr) : Double.MAX_VALUE;

        //  si precio actual es menor al de redis, se guarda en db
       if (event.getPrice() < lastPrice) {
            log.info("🎯 ¡Oferta! {} bajó a ${}", event.getProductName(), event.getPrice());

            PriceHistory history = new PriceHistory();
            history.setProductId(event.getProductId());
            history.setPrice(event.getPrice());
            history.setTimestamp(new Date());
            priceRepository.save(history);

            // actualiza redis con el nuevo precio
            redisTemplate.opsForValue().set(redisKey, event.getPrice().toString());

        } else {
           log.debug("No hay cambios para {}", event.getProductName() );

        }
    }

}
