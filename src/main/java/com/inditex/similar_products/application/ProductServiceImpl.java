package com.inditex.similar_products.application;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.port.ProductService;
import com.inditex.similar_products.infraestructure.external.ExternalProductApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ExternalProductApiClient apiClient;
    private final LoadingCache<String, Product> productCache;

    @Override
    public List<Product> getSimilarProducts(String productId) {
        List<String> similarProductIds = apiClient.getSimilarProductIds(productId);

        if (similarProductIds == null || similarProductIds.isEmpty()) {
            return List.of();
        }

        return similarProductIds.stream()
                .map(id -> {
                    try {
                        return productCache.get(id);
                    } catch (Exception e) {
                        log.warn("Failed to get product from cache for id {}: {}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
