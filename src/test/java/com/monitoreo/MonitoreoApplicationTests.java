package com.monitoreo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.mock;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration.class,
        org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration.class,
        org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration.class,
        org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.main.lazy-initialization=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.auto-create=false",
        "spring.data.mongodb.port=0",
        "spring.data.redis.port=0"
})
class MonitoreoApplicationTests {

    @Test
    void contextLoads() {
    }

}