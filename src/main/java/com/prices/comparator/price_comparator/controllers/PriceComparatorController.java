package com.prices.comparator.price_comparator.controllers;

import com.prices.comparator.price_comparator.dto.ProductResponse;
import com.prices.comparator.price_comparator.services.PriceComparatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comparator")
@CrossOrigin(origins = "http://localhost:4200") // Dominio permitido
public class PriceComparatorController {



    @Autowired
    private PriceComparatorService priceComparatorService;

    @GetMapping("/search")
    public ProductResponse searchProducts(@RequestParam String query) {
        return priceComparatorService.searchAllProducts(query);
    }


}
