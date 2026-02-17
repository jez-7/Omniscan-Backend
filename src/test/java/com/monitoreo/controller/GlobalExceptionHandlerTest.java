package com.monitoreo.controller;

import com.monitoreo.exception.GlobalExceptionHandler;
import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceRepository priceRepository;

    @Test
    void whenMethodArgumentTypeMismatch_thenReturnsError() throws Exception {
        mockMvc.perform(get("/api/prices/invalid-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void whenInternalError_thenReturnsInternalServerError() throws Exception {

        when(priceRepository.findAll()).thenThrow(new RuntimeException("Error de DB inesperado"));

        mockMvc.perform(get("/api/prices"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocurrió un error interno. Por favor, intente de nuevo más tarde."));
    }

}
