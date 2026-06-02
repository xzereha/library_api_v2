package com.example.libraryapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
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
                INSERT INTO library_user (username, password, role)
                    VALUES ('admin', '$2a$10$CvGgVuJ/Nu7DC8We55wsHOnHFFg8Ir.FyudEs9lZ3vLtC2qMJoksG', 'ADMIN');
                INSERT INTO library_user (username, password, role)
                    VALUES ('user', '$2a$10$mxIxv.BR1yPlbrbz59rGYuQ.Z9qeCRVwiuFftxr8EWMASA8Xnm.O.', 'USER');
                """)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationIntegrationTest {

    @Autowired private MockMvc mockMvc;

    private static String basicAuth(String username, String password) {
        return "Basic "
                + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Test
    @DisplayName("should allow access to public endpoints without auth")
    void shouldAllowPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/books")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("should require ADMIN for /secret endpoint")
    void shouldRequireAdminForSecret() throws Exception {
        mockMvc.perform(get("/api/v1/secret")).andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/v1/secret")
                                .header(HttpHeaders.AUTHORIZATION, basicAuth("user", "user123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/v1/secret")
                                .header(HttpHeaders.AUTHORIZATION, basicAuth("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should require ADMIN for swagger docs")
    void shouldRequireAdminForSwagger() throws Exception {
        mockMvc.perform(get("/api-docs").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should require ADMIN for H2 console")
    void shouldRequireAdminForH2() throws Exception {
        mockMvc.perform(get("/h2-console")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should lock account after 5 failed login attempts")
    void shouldLockAfterFailedAttempts() throws Exception {
        String auth = basicAuth("user", "wrongpass");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/loans").header(HttpHeaders.AUTHORIZATION, auth));
        }

        mockMvc.perform(get("/api/v1/loans").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isLocked());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("should return security headers on all responses")
    void shouldReturnSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }
}
