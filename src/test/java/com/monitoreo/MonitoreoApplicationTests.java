package com.monitoreo;

import com.monitoreo.repository.PriceRepository;
import com.monitoreo.service.PriceConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.mock;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        DataMongoAutoConfiguration.class,
        DataRedisAutoConfiguration.class,
        DataRedisAutoConfiguration.class,
        KafkaAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.main.lazy-initialization=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.auto-create=false",
        "spring.data.mongodb.port=0",
        "spring.data.redis.port=0"
})
@Import(MonitoreoApplicationTests.Config.class)
class MonitoreoApplicationTests {

    @TestConfiguration
    static class Config {
        @Bean
        public PriceRepository priceRepository() {
            return Mockito.mock(PriceRepository.class);
        }
        @Bean
        public MongoTemplate mongoTemplate() {
            return Mockito.mock(MongoTemplate.class);
        }
        @Bean
        public StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }

    @Test
    void contextLoads() {

    }
}