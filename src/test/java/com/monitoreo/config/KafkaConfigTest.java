package com.monitoreo.config;

import org.springframework.context.ApplicationContext;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@EmbeddedKafka(partitions = 1)
class KafkaConfigTest {
    @Autowired
    private ApplicationContext context;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.monitoreo.repository.PriceRepository priceRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.monitoreo.repository.SubscriptionRepository subscriptionRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.telegram.telegrambots.meta.TelegramBotsApi telegramBotsApi;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.monitoreo.bot.OmniscanTelegramBot omniscanTelegramBot;

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
