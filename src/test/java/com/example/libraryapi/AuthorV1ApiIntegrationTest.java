package com.example.libraryapi;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the v1 author endpoints. */
@Sql(
        statements =
                """
                DELETE FROM book;
                DELETE FROM author;
                ALTER TABLE book ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE author ALTER COLUMN id RESTART WITH 1;
                """,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
public class AuthorV1ApiIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /api/v1/authors")
    class CreateAuthorTests {
        @Test
        @DisplayName("should create a new author and return 201 Created")
        void shouldCreateAuthor() throws Exception {
            String requestBody =
                    """
                    {
                        "name": "J.R.R. Tolkien",
                        "isni": "0000 0001 2135 1230"
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/authors")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").value("J.R.R. Tolkien"));
        }

        @Test
        @DisplayName("should return 400 Bad Request when name is missing")
        void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
            String requestBody =
                    """
                    {
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/authors")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/authors")
    class GetAuthorsTests {
        @Sql(
                statements =
                        """
                        DELETE FROM author;
                        ALTER TABLE author ALTER COLUMN id RESTART WITH 1;
                        INSERT INTO author (name) VALUES
                        ('George Orwell'),
                        ('Aldous Huxley'),
                        ('J.R.R. Tolkien');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return a list of authors")
        void shouldReturnAllAuthors() throws Exception {
            mockMvc.perform(get("/api/v1/authors").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(
                            jsonPath(
                                    "$.data[*].name",
                                    hasItems("George Orwell", "Aldous Huxley", "J.R.R. Tolkien")));
        }

        @Test
        void shouldReturnEmptyListWhenNoAuthors() throws Exception {
            mockMvc.perform(get("/api/v1/authors").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name, isni) VALUES (1, 'George Orwell', '0000000121351230');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return author when filtering by valid ISNI")
        void shouldReturnAuthorByIsni() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors?isni=0000000121351230")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("George Orwell"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("should return empty page when filtering by non-existent ISNI")
        void shouldReturnEmptyPageForUnknownIsni() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors?isni=0000000000000000")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/authors/{id}")
    class GetAuthorByIdTests {
        @Sql(
                statements =
                        """
                        DELETE FROM author;
                        ALTER TABLE author ALTER COLUMN id RESTART WITH 1;
                        INSERT INTO author (id, name) VALUES
                        (1, 'George Orwell'),
                        (2, 'Aldous Huxley');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return author details for a valid ID")
        void shouldReturnAuthorById() throws Exception {
            mockMvc.perform(get("/api/v1/authors/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("George Orwell"));
        }

        @Test
        @DisplayName("should return 404 Not Found for non-existent ID")
        void shouldReturnNotFoundForNonExistentId() throws Exception {
            mockMvc.perform(get("/api/v2/authors/999").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/authors/{id}")
    class DeleteAuthorTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should delete an existing author and return 204 No Content")
        void shouldDeleteAuthorAndReturn204() throws Exception {
            mockMvc.perform(delete("/api/v1/authors/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
            mockMvc.perform(get("/api/v1/authors/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return 404 Not Found when deleting non-existent author")
        void shouldReturn404WhenAuthorNotFound() throws Exception {
            mockMvc.perform(delete("/api/v1/authors/999").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
