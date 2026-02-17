package com.monitoreo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@TestPropertySource(properties = {
        "spring.data.mongodb.auto-index-creation=false",
        "spring.data.redis.repositories.enabled=false"
})
class MonitoreoApplicationTests {

	@Test
	void contextLoads() {
	}

}
