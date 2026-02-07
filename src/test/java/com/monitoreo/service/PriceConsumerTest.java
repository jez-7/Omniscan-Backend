package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PriceConsumer priceConsumer;

    @Test
    void testConsumePriceEvent() {

        PriceEvent event = new PriceEvent("1", "RAM 8GB", 100.0, "link", "img", System.currentTimeMillis());

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        when(listOperations.range(anyString(), anyLong(), anyLong()))
                .thenReturn(List.of("200.0", "200.0"));

        priceConsumer.consumePriceEvent(event);

        verify(priceRepository, times(1)).save(any(PriceHistory.class));

        verify(notificationService, times(1)).sendTelegramAlert(anyString(), anyDouble(), anyString());

    }


}
