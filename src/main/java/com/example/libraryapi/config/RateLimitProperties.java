package com.example.libraryapi.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for per-IP rate limiting.
 *
 * <p>Supports per-endpoint overrides for /books, /authors, and /loans. Falls back to
 * {@link #defaultCapacity} and {@link #defaultRefillPerMinute} when no endpoint-specific config
 * is provided.
 */
@Data
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private int defaultCapacity = 100;
    private int defaultRefillPerMinute = 60;
    private Map<String, EndpointConfig> endpoints = new HashMap<>();

    /**
     * Resolve the configuration for a given endpoint name.
     *
     * @param endpoint the endpoint name (e.g. "books", "authors", "loans")
     * @return the endpoint-specific config if defined, otherwise the default config
     */
    public EndpointConfig resolve(String endpoint) {
        var fallback = new EndpointConfig(defaultCapacity, defaultRefillPerMinute);
        return endpoints.getOrDefault(endpoint, fallback);
    }

    /** Per-endpoint rate limit settings. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointConfig {
        private int capacity = 50;
        private int refillPerMinute = 30;
    }
}
