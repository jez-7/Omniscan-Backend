package com.monitoreo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Test
    void testSendTelegramAlert_Success() {
        NotificationService service = new NotificationService();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(service, "botToken", "fake-token");
        ReflectionTestUtils.setField(service, "chatId", "12345");

        when(mockRestTemplate.getForObject(anyString(), eq(String.class))).thenReturn("ok");

        service.sendTelegramAlert("Laptop Pro", 999.99, "https://example.com/product/1");

        verify(mockRestTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void testSendTelegramAlert_Error() {
        NotificationService service = new NotificationService();

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(service, "botToken", "fake-token");
        ReflectionTestUtils.setField(service, "chatId", "12345");

        when(mockRestTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        service.sendTelegramAlert("Laptop Pro", 999.99, "https://example.com/product/1");

        verify(mockRestTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }
}
