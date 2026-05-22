package com.example.libraryapi.config;

import com.example.libraryapi.config.RateLimitProperties.EndpointConfig;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Per-IP rate limiting filter using Bucket4J token bucket algorithm.
 *
 * <p>Applies to all {@code /api/v1/*} endpoints. Rate limits are configured per endpoint group
 * (books, authors, loans) via {@link RateLimitProperties}. Exceeds result in HTTP 429 with a {@code
 * Retry-After} header.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final String HEADER_RETRY_AFTER = "Retry-After";
    private static final String HEADER_X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String HEADER_X_RATELIMIT_RESET = "X-RateLimit-Reset";

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param properties the rate limit configuration
     */
    public RateLimitingFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String ip = extractIp(request);
        String endpoint = resolveEndpoint(request.getRequestURI());
        EndpointConfig config = properties.resolve(endpoint);
        String bucketKey = ip + ":" + endpoint;

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> newBucket(config));

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader(
                    HEADER_X_RATELIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
            response.setStatus(429);
            response.setHeader(HEADER_RETRY_AFTER, String.valueOf(waitSeconds));
            response.setHeader(HEADER_X_RATELIMIT_REMAINING, "0");
            long resetEpoch = Instant.now().getEpochSecond() + waitSeconds;
            response.setHeader(HEADER_X_RATELIMIT_RESET, String.valueOf(resetEpoch));
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter()
                    .write(
                            "{\"status\":429,\"error\":\"Too Many Requests\","
                                    + "\"message\":\"Rate limit exceeded for "
                                    + endpoint
                                    + ". Try again in "
                                    + waitSeconds
                                    + " seconds.\"}");
            LOG.warn("Rate limit exceeded for IP {} on {}", ip, endpoint);
        }
    }

    private static String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String resolveEndpoint(String uri) {
        if (uri.contains("/loans")) {
            return "loans";
        }
        if (uri.contains("/authors")) {
            return "authors";
        }
        if (uri.contains("/books")) {
            return "books";
        }
        return "default";
    }

    private static Bucket newBucket(EndpointConfig config) {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(config.getCapacity())
                                .refillGreedy(config.getRefillPerMinute(), Duration.ofMinutes(1))
                                .build())
                .build();
    }
}
