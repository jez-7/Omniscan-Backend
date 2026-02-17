package com.monitoreo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;

import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;

import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@EmbeddedKafka(partitions = 1)
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration.class,
        org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration.class,
        org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration.class,
        org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration.class
})
@TestPropertySource(properties = {
        "spring.main.lazy-initialization=true",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.data.mongodb.port=0",
        "spring.data.redis.port=0"
})
class MonitoreoApplicationTests {

	@Test
	void contextLoads() {
	}

}
