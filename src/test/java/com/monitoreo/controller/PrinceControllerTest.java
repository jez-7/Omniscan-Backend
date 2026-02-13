package com.monitoreo.controller;

import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
public class PrinceControllerTest {

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

}
