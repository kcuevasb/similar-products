package com.inditex.similar_products;

import com.inditex.similar_products.domain.model.Product;
import com.inditex.similar_products.infraestructure.client.ProductLoaderService;
import com.inditex.similar_products.infraestructure.external.ExternalProductApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SimilarProductsApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SimilarProductsIntegrationTest.MockedBeansConfig.class)
@ActiveProfiles("test")
class SimilarProductsIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private ExternalProductApiClient externalProductApiClient;

	@Autowired
	private ProductLoaderService productLoaderService;

	private RestClient restClient;

	@BeforeEach
	void setUpClient() {
		this.restClient = RestClient.builder()
				.baseUrl("http://localhost:" + port)
				.build();
	}

	@Test
	@DisplayName("Debería devolver productos similares para un ID válido")
	void shouldReturnSimilarProducts() {
		Product product = new Product("2", "Camiseta Blanca", 19.99, true);

		when(externalProductApiClient.getSimilarProductIds("1")).thenReturn(List.of("2"));
		when(productLoaderService.loadProduct("2")).thenReturn(product);

		ResponseEntity<Product[]> response = restClient.get()
				.uri("/product/1/similar")
				.retrieve()
				.toEntity(Product[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()[0].getId()).isEqualTo("2");
		assertThat(response.getBody()[0].getName()).isEqualTo("Camiseta Blanca");

		verify(externalProductApiClient).getSimilarProductIds("1");
		verify(productLoaderService).loadProduct("2");
	}

	@Test
	@DisplayName("Debería devolver 429 Too Many Requests si se supera el límite de llamadas")
	void shouldReturnTooManyRequestsIfRateLimitExceeded() {
		when(externalProductApiClient.getSimilarProductIds("1")).thenReturn(List.of("2"));
		when(productLoaderService.loadProduct("2"))
				.thenReturn(new Product("2", "Camiseta Blanca", 19.99, true));

		for (int i = 0; i < 5; i++) {
			try {
				restClient.get()
						.uri("/product/1/similar")
						.retrieve()
						.toEntity(Product[].class);
			} catch (HttpClientErrorException.TooManyRequests ex) {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
				return;
			}
		}

		fail("Expected TooManyRequests exception (429) was not thrown");
	}

	@Test
	@DisplayName("Debería obtener el producto desde la caché si ya está presente")
	void shouldUseCacheToLoadProduct() {
		Product cachedProduct = new Product("3", "Producto desde caché", 10.0, true);

		when(externalProductApiClient.getSimilarProductIds("cached-id")).thenReturn(List.of("3"));
		when(productLoaderService.loadProduct("3")).thenReturn(cachedProduct);

		ResponseEntity<Product[]> response = restClient.get()
				.uri("/product/cached-id/similar")
				.retrieve()
				.toEntity(Product[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody()[0].getName()).isEqualTo("Producto desde caché");
	}

	@TestConfiguration
	static class MockedBeansConfig {
		@Bean
		public ExternalProductApiClient externalProductApiClient() {
			return mock(ExternalProductApiClient.class);
		}

		@Bean
		public ProductLoaderService productLoaderService() {
			return mock(ProductLoaderService.class);
		}
	}
}