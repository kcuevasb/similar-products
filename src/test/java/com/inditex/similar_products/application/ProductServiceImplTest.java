package com.inditex.similar_products.application;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.external.ExternalProductApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ExternalProductApiClient apiClient;

    @Mock
    private LoadingCache<String, Product> productCache;

    @InjectMocks
    private ProductServiceImpl service;

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        productA = new Product("a", "Product A", 10.0, true);
        productB = new Product("b", "Product B", 20.0, true);
    }

    @Test
    void shouldReturnProducts_whenSimilarIdsAreFound() {
        String productId = "123";
        List<String> similarIds = List.of("a", "b", "c");

        when(apiClient.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productCache.get("a")).thenReturn(productA);
        when(productCache.get("b")).thenReturn(productB);
        when(productCache.get("c")).thenThrow(new RuntimeException("Not found"));

        List<Product> result = service.getSimilarProducts(productId);

        assertEquals(2, result.size());
        assertTrue(result.contains(productA));
        assertTrue(result.contains(productB));
    }

    @Test
    void shouldReturnEmptyList_whenNoSimilarIds() {
        String productId = "456";

        when(apiClient.getSimilarProductIds(productId)).thenReturn(List.of());

        List<Product> result = service.getSimilarProducts(productId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreFailedCacheLoads() {
        String productId = "789";
        List<String> similarIds = List.of("x", "y");

        when(apiClient.getSimilarProductIds(productId)).thenReturn(similarIds);
        when(productCache.get("x")).thenThrow(new RuntimeException("fail x"));
        when(productCache.get("y")).thenThrow(new RuntimeException("fail y"));

        List<Product> result = service.getSimilarProducts(productId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowException_whenClientFails() {
        String productId = "999";

        when(apiClient.getSimilarProductIds(productId)).thenThrow(new ProductNotFoundException("Client error"));

        assertThrows(ProductNotFoundException.class, () -> service.getSimilarProducts(productId));
    }

}