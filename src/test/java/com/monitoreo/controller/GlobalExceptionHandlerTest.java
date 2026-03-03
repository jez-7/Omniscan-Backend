package com.monitoreo.controller;

import com.monitoreo.exception.GlobalExceptionHandler;
import com.monitoreo.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceRepository priceRepository;

    @Test
    void whenTypeMismatch_thenReturnsBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        var request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/prices");

        var response = handler.handleTypeMismatch(ex, request);

        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/prices", response.getBody().getPath());


        assertTrue(response.getBody().getMessage().contains("id"));

        assertFalse(response.getBody().getMessage().contains("abc"),
                "El mensaje de error no debe reflejar el valor de entrada para prevenir XSS");

        assertTrue(response.getBody().getMessage().contains("formato incorrecto"));
    }

    @Test
    void testNoHandlerFoundExceptionDirectly() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var ex = mock(org.springframework.web.servlet.NoHandlerFoundException.class);
        var request = mock(jakarta.servlet.http.HttpServletRequest.class);

        when(ex.getRequestURL()).thenReturn("/api/unknown");
        when(request.getRequestURI()).thenReturn("/api/unknown");

        var response = handler.noHandlerFoundException(ex, request);

        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("/api/unknown"));
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
