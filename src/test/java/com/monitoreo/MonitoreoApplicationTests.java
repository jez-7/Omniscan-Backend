package com.monitoreo;

import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
                "spring.main.lazy-initialization=true"
        }
)

class MonitoreoApplicationTests {

    @MockitoBean
    PriceRepository priceRepository;

    @MockitoBean
    MongoTemplate mongoTemplate;

    @MockitoBean
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {

    }
}