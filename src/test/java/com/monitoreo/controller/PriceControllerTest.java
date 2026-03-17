package com.monitoreo.controller;

import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceRepository priceRepository;

    @Test
    void returnListOfPrices() throws Exception {

        when(priceRepository.findAll()).thenReturn(List.of(new PriceHistory()));

        mockMvc.perform(get("/api/prices").header("API-Version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    void returnPricesByProductId() throws Exception {
        PriceHistory p1 = PriceHistory.builder()
                .productId("1")
                .productName("Laptop Pro")
                .price(999.99)
                .timestamp(new Date())
                .permalink("https://example.com/1")
                .thumbnail("thumb1.jpg")
                .build();

        PriceHistory p2 = PriceHistory.builder()
                .productId("2")
                .productName("Mouse")
                .price(29.99)
                .timestamp(new Date())
                .permalink("https://example.com/2")
                .thumbnail("thumb2.jpg")
                .build();

        PriceHistory p3 = PriceHistory.builder()
                .productId("1")
                .productName("Laptop Pro")
                .price(899.99)
                .timestamp(new Date())
                .permalink("https://example.com/1")
                .thumbnail("thumb1.jpg")
                .build();

        when(priceRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        mockMvc.perform(get("/api/prices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value("1"))
                .andExpect(jsonPath("$[1].productId").value("1"));
    }

    @Test
    void returnEmptyListForNonExistentProduct() throws Exception {
        when(priceRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/prices/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
