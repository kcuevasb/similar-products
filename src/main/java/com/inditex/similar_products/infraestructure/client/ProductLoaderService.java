package com.inditex.similar_products.infraestructure.client;
import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.external.RestApiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductLoaderService {

    private final RestApiClient restApiClient;

    @CircuitBreaker(name = "productApiCircuitBreaker", fallbackMethod = "fallbackLoad")
    @Retry(name = "productApiCircuitBreaker")
    public Product loadProduct(String productId) {
        log.info("Calling external API for productId={}", productId);
        try {
            Product product = restApiClient.get("/product/{id}", Product.class, productId);
            log.debug("Successfully loaded product: {}", product);
            return product;
        } catch (Exception e) {
            throw new ProductNotFoundException(productId, e);
        }
    }

    public Product fallbackLoad(String productId, Throwable ex) {
        log.warn("Fallback for loading product - productId={}, reason={}", productId, ex.toString());
        return null;
    }
}
