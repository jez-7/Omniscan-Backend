package com.monitoreo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import static org.mockito.Mockito.mock;


@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class MonitoreoApplicationTests {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public MongoTemplate mongoTemplate() {
            return mock(MongoTemplate.class);
        }

        @Bean
        @Primary
        public RedisTemplate<String, Object> redisTemplate() {
            return mock(RedisTemplate.class);
        }

        @Bean
        @Primary
        public KafkaTemplate<String, Object> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }

    @Test
    void contextLoads() {
    }

}