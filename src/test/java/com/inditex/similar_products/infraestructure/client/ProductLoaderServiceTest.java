package com.inditex.similar_products.infraestructure.client;

import com.inditex.similar_products.domain.exception.custom.ProductNotFoundException;
import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.external.RestApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLoaderServiceTest {

    @Mock
    private RestApiClient restApiClient;

    @InjectMocks
    private ProductLoaderService service;

    @Test
    void shouldReturnProduct_whenRestApiClientReturnsSuccessfully() {
        Product expected = new Product("1", "Product 1", 10.0, true);
        when(restApiClient.get("/product/{id}", Product.class, "1")).thenReturn(expected);

        Product result = service.loadProduct("1");

        assertNotNull(result);
        assertEquals(expected, result);
        verify(restApiClient).get("/product/{id}", Product.class, "1");
    }

    @Test
    void shouldThrowProductNotFoundException_whenRestApiClientThrows() {
        when(restApiClient.get("/product/{id}", Product.class, "404"))
                .thenThrow(new RuntimeException("simulated failure"));

        assertThrows(ProductNotFoundException.class, () -> service.loadProduct("404"));
    }

    @Test
    void fallbackLoad_shouldLogWarningWithoutThrowing() {
        assertDoesNotThrow(() ->
                service.fallbackLoad("fallback-id", new RuntimeException("simulated"))
        );
    }

}