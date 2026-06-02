package com.example.libraryapi;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for per-IP rate limiting via Bucket4J. */
@Sql(
        statements =
                """
                DELETE FROM loan;
                DELETE FROM library_user;
                DELETE FROM book;
                DELETE FROM author;
                ALTER TABLE loan ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE library_user ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE book ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE author ALTER COLUMN id RESTART WITH 1;
                INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                """,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@SpringBootTest(
        properties = {
            "spring.cache.type=none",
            "app.rate-limit.default-capacity=2",
            "app.rate-limit.default-refill-per-minute=1",
            "app.rate-limit.endpoints.authors.capacity=1",
            "app.rate-limit.endpoints.authors.refill-per-minute=1",
        })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RateLimitingIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("should include rate limit headers on successful requests")
    void shouldIncludeRateLimitHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", notNullValue()));

        mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    @Test
    @DisplayName("should return 429 with Retry-After when rate limit exceeded")
    void shouldReturn429WhenExceeded() throws Exception {
        var url = "/api/v1/books/1";

        mockMvc.perform(get(url)).andExpect(status().isOk());
        mockMvc.perform(get(url)).andExpect(status().isOk());

        mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", notNullValue()))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Reset", notNullValue()))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value(containsString("books")));
    }

    @Test
    @DisplayName("should enforce independent rate limits per endpoint")
    void shouldEnforceIndependentLimitsPerEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/books/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/books/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/books/1")).andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/v1/authors/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/authors/1")).andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should not rate limit non-API paths")
    void shouldNotRateLimitNonApiPaths() throws Exception {
        mockMvc.perform(get("/api-docs").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
