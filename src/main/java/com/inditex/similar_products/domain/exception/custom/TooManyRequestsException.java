package com.inditex.similar_products.domain.exception.custom;

public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String productId) {
        super("Too many requests for product ID: " + productId);
    }

    public TooManyRequestsException(String productId, Throwable cause) {
        super("Too many requests for product ID: " + productId, cause);
    }
}
