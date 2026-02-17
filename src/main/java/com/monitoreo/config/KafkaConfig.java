package com.monitoreo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic priceTopic() {
        return TopicBuilder.name("prices-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }
}
