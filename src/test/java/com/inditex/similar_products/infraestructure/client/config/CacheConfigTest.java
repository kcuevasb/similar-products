package com.inditex.similar_products.infraestructure.client.config;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.client.ProductLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    private ProductLoaderService productLoaderService;
    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        productLoaderService = mock(ProductLoaderService.class);
        cacheConfig = new CacheConfig();
    }

    @Test
    @DisplayName("Debería crear un bean de caché con configuración válida")
    void shouldCreateProductCacheBean() {
        LoadingCache<String, Product> cache = cacheConfig.productCache(productLoaderService);

        assertNotNull(cache);
    }

    @Test
    @DisplayName("Debería cargar el producto usando el ProductLoaderService")
    void shouldLoadProductUsingLoaderService() {
        String productId = "123";
        Product product = new Product(productId, "Camiseta", 19.99, true);

        when(productLoaderService.loadProduct(productId)).thenReturn(product);

        LoadingCache<String, Product> cache = cacheConfig.productCache(productLoaderService);
        Product result = cache.get(productId);

        assertEquals(product, result);
        verify(productLoaderService, times(1)).loadProduct(productId);
    }

    @Test
    @DisplayName("Debería usar la caché tras la primera carga")
    void shouldUseCacheAfterFirstLoad() {
        String productId = "123";
        Product product = new Product(productId, "Camiseta", 19.99, true);

        when(productLoaderService.loadProduct(productId)).thenReturn(product);

        LoadingCache<String, Product> cache = cacheConfig.productCache(productLoaderService);

        Product firstLoad = cache.get(productId);
        Product secondLoad = cache.get(productId);

        assertSame(firstLoad, secondLoad);
        verify(productLoaderService, times(1)).loadProduct(productId); // solo se llama una vez
    }

}