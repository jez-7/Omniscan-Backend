package com.monitoreo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;


@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@ImportAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        DataMongoAutoConfiguration.class,
        DataRedisReactiveAutoConfiguration.class,
        DataRedisRepositoriesAutoConfiguration.class
})
class MonitoreoApplicationTests {

	@Test
	void contextLoads() {
	}

}
