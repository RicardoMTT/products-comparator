package com.prices.comparator.price_comparator.dto;

import java.util.List;
import java.util.Map;

public class ProductResponse {

    private List<Map<String, String>> tottusProducts;
    private List<Map<String, String>> tamboProducts;

    public ProductResponse(List<Map<String, String>> tottusProducts, List<Map<String, String>> tamboProducts) {
        this.tottusProducts = tottusProducts;
        this.tamboProducts = tamboProducts;
    }

    public List<Map<String, String>> getTottusProducts() {
        return tottusProducts;
    }

    public void setTottusProducts(List<Map<String, String>> tottusProducts) {
        this.tottusProducts = tottusProducts;
    }

    public List<Map<String, String>> getTamboProducts() {
        return tamboProducts;
    }

    public void setTamboProducts(List<Map<String, String>> tamboProducts) {
        this.tamboProducts = tamboProducts;
    }
}
