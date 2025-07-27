package com.inditex.similar_products.infraestructure.external;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalProductApiClient {

    private final RestApiClient restApiClient;
    private final LoadingCache<String, Product> productCache;

    @CircuitBreaker(name = "productApiCircuitBreaker", fallbackMethod = "fallbackGetSimilarProductIds")
    @Retry(name = "productApiCircuitBreaker")
    public List<String> getSimilarProductIds(String productId) {
        try {
            String[] response = restApiClient.get("/product/{id}/similarids", String[].class, productId);
            if (response == null) {
                throw new ProductNotFoundException("No similar products found for ID: " + productId);
            }
            return List.of(response);
        } catch (Exception e) {
            throw new ProductNotFoundException("Error fetching similar product IDs for id: " + productId, e);
        }
    }

    public Product getProductById(String productId) {
        try {
            return productCache.get(productId);
        } catch (Exception e) {
            log.warn("Error getting product from cache: {}", e.getMessage());
            throw new ProductNotFoundException(productId, e);
        }
    }

    public List<String> fallbackGetSimilarProductIds(String productId, Throwable ex) {
        log.warn("Fallback for getSimilarProductIds - productId={}, reason={}", productId, ex.toString());
        throw new ProductNotFoundException(productId);
    }
}
