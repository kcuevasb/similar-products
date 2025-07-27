package com.inditex.similar_products.infraestructure.client.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.client.ProductLoaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public LoadingCache<String, Product> productCache(ProductLoaderService productLoaderService) {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(productLoaderService::loadProduct);
    }
}
