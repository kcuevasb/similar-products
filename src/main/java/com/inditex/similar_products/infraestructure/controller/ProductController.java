package com.inditex.similar_products.infraestructure.controller;

import com.inditex.similar_products.domain.exception.custom.TooManyRequestsException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.port.ProductService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}/similar")
    @RateLimiter(name = "productApiRateLimiter", fallbackMethod = "fallbackTooManyRequests")
    public ResponseEntity<List<Product>> getSimilarProducts(@PathVariable String productId) {
        log.info("Received request to get similar products for ID: {}", productId);
        List<Product> products = productService.getSimilarProducts(productId);
        return ResponseEntity.ok(products);
    }

    public ResponseEntity<List<Product>> fallbackTooManyRequests(String productId, RequestNotPermitted ex) {
        log.warn("Too many requests for productId={}, reason={}", productId, ex.getMessage());
        throw new TooManyRequestsException("Too many requests for productId: " + productId, ex);
    }
}
