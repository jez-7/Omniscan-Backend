package com.monitoreo;

import com.monitoreo.repository.PriceRepository;
import com.monitoreo.service.NotificationService;
import com.monitoreo.service.PriceConsumer;
import com.monitoreo.service.PriceProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration," +
                        "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration," +
                        "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration," +
                        "org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration," +
                        "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
                "spring.main.lazy-initialization=true"
        }
)

class MonitoreoApplicationTests {

    @MockitoBean PriceRepository priceRepository;
    @MockitoBean MongoTemplate mongoTemplate;
    @MockitoBean StringRedisTemplate stringRedisTemplate;


    @MockitoBean
    PriceProducer priceProducer;
    @MockitoBean
    PriceConsumer priceConsumer;
    @MockitoBean
    NotificationService notificationService;

    @Test
    void contextLoads() {

    }
}