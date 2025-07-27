package com.inditex.similar_products.domain.port;

import com.inditex.similar_products.domain.model.Product;

import java.util.List;

public interface ProductService {

    List<Product> getSimilarProducts(String productId);
}
