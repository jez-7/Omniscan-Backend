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
public class KafkaConfigTest {
    @Autowired
    private ApplicationContext context;

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
