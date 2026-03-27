package com.monitoreo.controller;

import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Prices", description = "Endpoints for querying the detected deals history")
public class PriceController {

    private final PriceRepository priceRepository;

    @Operation(summary = "Get all detected deals", description = "Returns a list with the complete history of detected deals")
    @GetMapping
    public List<PriceHistory> getAllPrices() {
        return priceRepository.findAll();
    }

    @Operation(summary = "Get deals by product", description = "Returns a list with the history of detected deals for a specific product")
    @GetMapping(value = "/{productId}")
    public List<PriceHistory> getPricesByProduct(@PathVariable String productId) {
        return priceRepository.findAll().stream()
                .filter(p -> p.getProductId().equals(productId))
                .toList();
    }


}
