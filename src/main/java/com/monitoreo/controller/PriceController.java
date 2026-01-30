package com.monitoreo.controller;

import com.monitoreo.model.entity.PriceHistory;
import com.monitoreo.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
public class PriceController {

    @Autowired
    private PriceRepository priceRepository;

    @GetMapping(version = "1")
    public List<PriceHistory> getAllPrices() {
        return priceRepository.findAll();
    }

    @GetMapping(value = "/{productId}", version = "1")
    public List<PriceHistory> getPricesByProduct(@PathVariable String productId) {
        return priceRepository.findAll().stream()
                .filter(p -> p.getProductId().equals(productId))
                .toList();
    }


}
