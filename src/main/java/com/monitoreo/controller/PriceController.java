package com.monitoreo.controller;

import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
@Tag(name = "Precios", description = "Endpoints para consultar el historial de ofertas detectadas")
public class PriceController {

    @Autowired
    private PriceRepository priceRepository;

    @Operation(summary = "Obtener todas las ofertas detectadas", description = "Devuelve una lista con el historial completo de ofertas detectadas")
    @GetMapping
    public List<PriceHistory> getAllPrices() {
        return priceRepository.findAll();
    }

    @Operation(summary = "Obtener ofertas por producto", description = "Devuelve una lista con el historial de ofertas detectadas para un producto específico")
    @GetMapping(value = "/{productId}")
    public List<PriceHistory> getPricesByProduct(@PathVariable String productId) {
        return priceRepository.findAll().stream()
                .filter(p -> p.getProductId().equals(productId))
                .toList();
    }


}
