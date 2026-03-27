package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.PriceRepository;
import com.monitoreo.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

/**
 * Service responsible for processing price events received from Kafka.
 * Calculates moving averages using Redis and determines whether a deal exists.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PriceConsumer {

    private final StringRedisTemplate redisTemplate;

    private final PriceRepository priceRepository;

    private final NotificationService notificationService;

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Method that consumes price events from the Kafka topic.
     * Stores prices in Redis to calculate the moving average and detects deals.
     *
     * @param event The price event received from Kafka.
     */
    @KafkaListener(topics = "prices-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePriceEvent(PriceEvent event) {

        String redisKey = "product:window:" + event.getProductId();

        redisTemplate.opsForList().leftPush(redisKey, event.getPrice().toString());
        redisTemplate.opsForList().trim(redisKey, 0, 9); // keep only the last 10 prices

        // retrieve all prices in the window and calculate the average
        List<String> prices = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (prices != null && !prices.isEmpty()) {

            double average = prices.stream()
                    .mapToDouble(Double::parseDouble)
                    .average()
                    .orElse(0.0);

            log.info("Item: {} | Current: ${} | Average: ${}",
                    event.getProductName(), event.getPrice(), String.format("%.2f", average));

            // if the price is 5% below the average, save it to the database
            if (event.getPrice() < (average * 0.95)) {
                log.info(" ---- DEAL DETECTED! ----");

                PriceHistory history = PriceHistory.builder()
                        .productId(event.getProductId())
                        .productName(event.getProductName())
                        .price(event.getPrice())
                        .timestamp(new Date(event.getTimestamp()))
                        .permalink(event.getPermalink())
                        .thumbnail(event.getThumbnail())
                        .build();

                priceRepository.save(history);

                List<Subscription> allSubscriptions = subscriptionRepository.findAll();

                for (Subscription sub : allSubscriptions) {

                    if (event.getProductName().toLowerCase().contains(sub.getKeyword().toLowerCase())) {

                        log.info("Notifying user {} about deal for {}", sub.getChatId(), event.getProductName());

                        notificationService.sendTelegramAlert(
                                sub.getChatId(),
                                event.getProductName(),
                                event.getPrice(),
                                event.getPermalink()
                        );
                    }
                }
            }
        }
    }

}
