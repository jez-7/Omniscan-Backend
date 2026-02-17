package com.monitoreo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
public class PriceProducerTest {

    @InjectMocks
    private PriceProducer priceProducer;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testFetchAndSimulateVolatility_Success() {
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> product = new HashMap<>();
        product.put("id", "1");
        product.put("title", "Laptop Pro");
        product.put("price", 1000.0);
        product.put("thumbnail", "thumb.jpg");
        mockResponse.put("products", List.of(product));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockResponse);

        priceProducer.fetchAndSimulateVolatility();

        verify(kafkaTemplate, times(1)).send(eq("prices-topic"), anyString(), any());
    }

    @Test
    void testFetchAndSimulateVolatility_Error() {

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Down"));

        priceProducer.fetchAndSimulateVolatility();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}
