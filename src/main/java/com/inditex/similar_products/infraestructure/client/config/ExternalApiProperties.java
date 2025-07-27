package com.inditex.similar_products.infraestructure.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "external.api")
public class ExternalApiProperties {
    private String baseUrl;
}
