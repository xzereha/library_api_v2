package com.example.libraryapi;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the v1 book endpoints. */
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
                """,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
public class BookV1ApiIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /api/v1/books")
    @WithMockUser(roles = "ADMIN")
    class CreateBookTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should create a new book and return 201 Created")
        void shouldCreateBook() throws Exception {
            String requestBody =
                    """
                    {
                        "title": "1984",
                        "isbn": "978-0-141-03714-5",
                        "authorId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", notNullValue()))
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.title").value("1984"))
                    .andExpect(jsonPath("$.data.isbn").value("9780141037145"))
                    .andExpect(jsonPath("$.data.authorName").value("George Orwell"))
                    .andExpect(jsonPath("$.data.authorId").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should create a book without ISBN and return 201 Created")
        void shouldCreateBookWithoutIsbn() throws Exception {
            String requestBody =
                    """
                    {
                        "title": "Animal Farm",
                        "authorId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.title").value("Animal Farm"))
                    .andExpect(jsonPath("$.data.isbn").doesNotExist())
                    .andExpect(jsonPath("$.data.authorName").value("George Orwell"));
        }

        @Test
        @DisplayName("should return 400 Bad Request when title is missing")
        void shouldReturn400WhenTitleIsMissing() throws Exception {
            String requestBody =
                    """
                    {
                        "authorId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 Bad Request when title is blank")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            String requestBody =
                    """
                    {
                        "title": "",
                        "authorId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return 400 Bad Request when authorId is zero")
        void shouldReturn400WhenAuthorIdIsZero() throws Exception {
            String requestBody =
                    """
                    {
                        "title": "1984",
                        "authorId": 0
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 Not Found when author does not exist")
        void shouldReturn404WhenAuthorDoesNotExist() throws Exception {
            String requestBody =
                    """
                    {
                        "title": "1984",
                        "authorId": 999
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 Bad Request when request body is empty")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            String requestBody = "{}";

            mockMvc.perform(
                            post("/api/v1/books")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books")
    class GetBooksTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (title, author_id) VALUES ('1984', 1);
                        INSERT INTO book (title, author_id) VALUES ('Animal Farm', 1);
                        INSERT INTO book (title, author_id) VALUES ('The Lord of the Rings', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return a paginated list of books")
        void shouldReturnPaginatedBooks() throws Exception {
            mockMvc.perform(get("/api/v1/books").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[*].title",
                            hasItems("1984", "Animal Farm", "The Lord of the Rings")))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("should return empty page when no books exist")
        void shouldReturnEmptyPageWhenNoBooks() throws Exception {
            mockMvc.perform(get("/api/v1/books").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (title, author_id) VALUES ('1984', 1);
                        INSERT INTO book (title, author_id) VALUES ('Animal Farm', 1);
                        INSERT INTO book (title, author_id) VALUES ('Homage to Catalonia', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should respect page size parameter")
        void shouldRespectPageSizeParameter() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?size=2").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (title, author_id) VALUES ('1984', 1);
                        INSERT INTO book (title, author_id) VALUES ('Animal Farm', 1);
                        INSERT INTO book (title, author_id) VALUES ('Homage to Catalonia', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return second page when page parameter is used")
        void shouldReturnSecondPage() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?page=1&size=2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, isbn, author_id) VALUES (1, '1984', '9780141037145', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (2, 'Animal Farm', '9780141036131', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (3, 'The Lord of the Rings', '9780544003415', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should filter books by exact ISBN")
        void shouldFilterByIsbn() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?isbn=9780141037145")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].title").value("1984"))
                    .andExpect(jsonPath("$.data[0].isbn").value("9780141037145"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, isbn, author_id) VALUES (1, '1984', '9780141037145', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (2, 'Animal Farm', '9780141036131', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (3, 'The Lord of the Rings', '9780544003415', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should filter books by title case-insensitive partial match")
        void shouldFilterByTitleCaseInsensitivePartial() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?title=lord of the")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].title").value("The Lord of the Rings"))
                    .andExpect(jsonPath("$.data[0].authorName").value("J.R.R. Tolkien"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, isbn, author_id) VALUES (1, '1984', '9780141037145', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (2, 'Animal Farm', '9780141036131', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (3, 'The Lord of the Rings', '9780544003415', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should filter books by author name case-insensitive partial match")
        void shouldFilterByAuthorNamePartial() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?authorName=ORWELL")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[*].title",
                            hasItems("1984", "Animal Farm")))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, isbn, author_id) VALUES (1, '1984', '9780141037145', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (2, 'Animal Farm', '9780141036131', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (3, 'The Lord of the Rings', '9780544003415', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should filter books by title and author name combined")
        void shouldFilterByTitleAndAuthorName() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?title=farm&authorName=orwell")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].title").value("Animal Farm"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, isbn, author_id) VALUES (1, '1984', '9780141037145', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (2, 'Animal Farm', '9780141036131', 1);
                        INSERT INTO book (id, title, isbn, author_id) VALUES (3, 'The Lord of the Rings', '9780544003415', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return empty list when no books match filters")
        void shouldReturnEmptyWhenNoBooksMatchFilters() throws Exception {
            mockMvc.perform(
                            get("/api/v1/books?title=nonexistent")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/books/{id}")
    class GetBookByIdTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return book details for a valid ID")
        void shouldReturnBookById() throws Exception {
            mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("1984"))
                    .andExpect(jsonPath("$.data.authorName").value("George Orwell"))
                    .andExpect(jsonPath("$.data.authorId").value(1));
        }

        @Test
        @DisplayName("should return 404 Not Found for non-existent book ID")
        void shouldReturn404WhenBookNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/books/999").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/authors/{authorId}/books")
    class GetBooksByAuthorTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (title, author_id) VALUES ('1984', 1);
                        INSERT INTO book (title, author_id) VALUES ('Animal Farm', 1);
                        INSERT INTO book (title, author_id) VALUES ('The Lord of the Rings', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return books by a specific author")
        void shouldReturnBooksByAuthor() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors/1/books")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(
                            jsonPath(
                                    "$.data[*].title",
                                    hasItems("1984", "Animal Farm")))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.data[0].authorName").value("George Orwell"));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (title, author_id) VALUES ('1984', 1);
                        INSERT INTO book (title, author_id) VALUES ('Animal Farm', 1);
                        INSERT INTO book (title, author_id) VALUES ('Homage to Catalonia', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return paginated results for books by author")
        void shouldRespectPageSizeForBooksByAuthor() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors/1/books?size=2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return empty page when author has no books")
        void shouldReturnEmptyPageWhenAuthorHasNoBooks() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors/1/books")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        @DisplayName("should return 404 Not Found when author does not exist")
        void shouldReturn404WhenAuthorNotFound() throws Exception {
            mockMvc.perform(
                            get("/api/v1/authors/999/books")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/books/{id}")
    @WithMockUser(roles = "ADMIN")
    class DeleteBookTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should delete an existing book and return 204 No Content")
        void shouldDeleteBookAndReturn204() throws Exception {
            mockMvc.perform(
                            delete("/api/v1/books/1")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
            mockMvc.perform(
                            get("/api/v1/books/1")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 404 Not Found when deleting non-existent book")
        void shouldReturn404WhenBookNotFound() throws Exception {
            mockMvc.perform(
                            delete("/api/v1/books/999")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
