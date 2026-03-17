package com.monitoreo.config;

import org.springframework.context.ApplicationContext;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                        "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration," +
                        "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration," +
                        "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration"
        }
)
@EmbeddedKafka(partitions = 1)
class KafkaConfigTest {
    @Autowired
    private ApplicationContext context;

    @MockitoBean
    com.monitoreo.repository.PriceRepository priceRepository;

    @MockitoBean
    com.monitoreo.repository.SubscriptionRepository subscriptionRepository;

    @MockitoBean
    org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @MockitoBean
    org.telegram.telegrambots.meta.TelegramBotsApi telegramBotsApi;

    @MockitoBean
    com.monitoreo.bot.OmniscanTelegramBot omniscanTelegramBot;

    @MockitoBean
    org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    com.monitoreo.service.NotificationService notificationService;

    @Test
    void testBeansAreRegistered() {
        NewTopic priceTopic = context.getBean(NewTopic.class);
        assertNotNull(priceTopic);
        assertEquals("prices-topic", priceTopic.name());
        assertEquals(1, priceTopic.numPartitions());

        RestTemplate restTemplate = context.getBean(RestTemplate.class);
        assertNotNull(restTemplate);
    }
}
