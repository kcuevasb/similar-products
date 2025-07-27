package com.inditex.similar_products.infraestructure.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RestApiClient {

    private final RestClient restClient;

    public <T> T get(String uri, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return restClient.get()
                .uri(uri, uriVariables)
                .retrieve()
                .body(responseType);
    }
}
