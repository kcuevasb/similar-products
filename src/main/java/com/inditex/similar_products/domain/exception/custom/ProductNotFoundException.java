package com.inditex.similar_products.domain.exception.custom;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String productId) {
        super("Product not found for ID: " + productId);
    }

    public ProductNotFoundException(String productId, Throwable cause) {
        super("Product not found for ID: " + productId, cause);
    }
}
