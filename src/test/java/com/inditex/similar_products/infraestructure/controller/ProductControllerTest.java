package com.inditex.similar_products.infraestructure.controller;

import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.domain.port.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(ProductControllerTest.MockedConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @TestConfiguration
    static class MockedConfig {
        @Bean
        public ProductService productService() {
            return mock(ProductService.class);
        }
    }

    @Test
    void shouldReturn200WithSimilarProducts() throws Exception {
        Product p1 = new Product("a", "Product A", 10.0, true);
        Product p2 = new Product("b", "Product B", 20.0, true);
        when(productService.getSimilarProducts("123")).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/product/123/similar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("a"))
                .andExpect(jsonPath("$[1].id").value("b"));
    }

    @Test
    void shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.getSimilarProducts("404")).thenThrow(new ProductNotFoundException("Not found"));

        mockMvc.perform(get("/product/404/similar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500_whenUnexpectedErrorOccurs() throws Exception {
        when(productService.getSimilarProducts("500")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/product/500/similar"))
                .andExpect(status().isInternalServerError());
    }
}