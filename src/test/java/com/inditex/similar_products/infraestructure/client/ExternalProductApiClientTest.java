package com.inditex.similar_products.infraestructure.client;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.external.ExternalProductApiClient;
import com.inditex.similar_products.infraestructure.external.RestApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    class ExternalProductApiClientTest {

        @Mock
        private RestApiClient restClient;

        @Mock
        private LoadingCache<String, Product> productCache;

        @InjectMocks
        private ExternalProductApiClient apiClient;

        @Test
        void shouldReturnSimilarProductIds_whenRestClientReturnsValidArray() {
            when(restClient.get("/product/{id}/similarids", String[].class, "123"))
                    .thenReturn(new String[]{"a", "b", "c"});

            List<String> result = apiClient.getSimilarProductIds("123");

            assertEquals(List.of("a", "b", "c"), result);
        }

        @Test
        void shouldThrowException_whenRestClientReturnsNull() {
            when(restClient.get("/product/{id}/similarids", String[].class, "123"))
                    .thenReturn(null);

            assertThrows(ProductNotFoundException.class, () -> apiClient.getSimilarProductIds("123"));
        }

        @Test
        void shouldThrowException_whenRestClientThrowsException() {
            when(restClient.get("/product/{id}/similarids", String[].class, "123"))
                    .thenThrow(new RuntimeException("fail"));

            assertThrows(ProductNotFoundException.class, () -> apiClient.getSimilarProductIds("123"));
        }

        @Test
        void shouldReturnProductFromCache() {
            Product p = new Product("123", "Sample", 9.99, true);
            when(productCache.get("123")).thenReturn(p);

            Product result = apiClient.getProductById("123");

            assertEquals(p, result);
        }

        @Test
        void shouldThrowException_whenCacheFails() {
            when(productCache.get("123")).thenThrow(new RuntimeException("cache down"));

            assertThrows(ProductNotFoundException.class, () -> apiClient.getProductById("123"));
        }

        @Test
        void fallbackShouldThrowProductNotFoundException() {
            RuntimeException cause = new RuntimeException("simulated failure");

            ProductNotFoundException ex = assertThrows(
                    ProductNotFoundException.class,
                    () -> apiClient.fallbackGetSimilarProductIds("123", cause)
            );

            assertTrue(ex.getMessage().contains("123"));
        }

}