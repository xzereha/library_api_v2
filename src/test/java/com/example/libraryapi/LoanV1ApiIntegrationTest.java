package com.example.libraryapi;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.libraryapi.exception.BookNotAvailableException;
import com.example.libraryapi.service.LoanService;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Integration tests for the v1 loan endpoints. */
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
                INSERT INTO library_user (id, username, password, role) VALUES (1, 'user', '{noop}user123', 'USER');
                INSERT INTO library_user (id, username, password, role) VALUES (2, 'admin', '{noop}admin123', 'ADMIN');
                """,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
public class LoanV1ApiIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /api/v1/loans")
    @WithMockUser(username = "user")
    class CreateLoanTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should borrow a book and return 201 Created")
        void shouldBorrowBook() throws Exception {
            String requestBody =
                    """
                    {
                        "bookId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/loans")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.username").value("user"))
                    .andExpect(jsonPath("$.data.loanDate").exists())
                    .andExpect(jsonPath("$.data.returnedDate").doesNotExist())
                    .andExpect(jsonPath("$.data.bookId").value(1))
                    .andExpect(jsonPath("$.data.bookTitle").value("1984"));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return 409 Conflict when book is already loaned")
        void shouldReturn409WhenBookAlreadyLoaned() throws Exception {
            String borrowRequest =
                    """
                    {
                        "bookId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/loans")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(borrowRequest))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post("/api/v1/loans")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(borrowRequest))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("should return 404 Not Found when book does not exist")
        void shouldReturn404WhenBookNotFound() throws Exception {
            String requestBody =
                    """
                    {
                        "bookId": 999
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/loans")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/loans/{id}/return")
    class ReturnBookTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 1, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return a book and set returned date")
        void shouldReturnBook() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/1/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("user"))
                    .andExpect(jsonPath("$.data.loanDate").value("2026-05-11"))
                    .andExpect(jsonPath("$.data.returnedDate").value(notNullValue()))
                    .andExpect(jsonPath("$.data.bookId").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, user_id, loan_date, returned_date, book_id) VALUES (1, 1, '2026-05-11', '2026-05-18', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should be idempotent when book is already returned")
        void shouldBeIdempotentWhenAlreadyReturned() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/1/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.returnedDate").value("2026-05-18"));
        }

        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return 404 Not Found when loan does not exist")
        void shouldReturn404WhenLoanNotFound() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/999/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO library_user (id, username, password, role) VALUES (3, 'otheruser', '{noop}user123', 'USER');
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 3, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return 403 Forbidden when returning another user's loan")
        void shouldReturn403WhenReturningOtherUsersLoan() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/1/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO library_user (id, username, password, role) VALUES (3, 'otheruser', '{noop}user123', 'USER');
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 3, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("should allow admin to return any user's loan")
        void shouldAllowAdminToReturnAnyLoan() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/1/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/loans/{id}")
    class GetLoanByIdTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 1, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return loan details for a valid ID")
        void shouldReturnLoanById() throws Exception {
            mockMvc.perform(get("/api/v1/loans/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("user"))
                    .andExpect(jsonPath("$.data.bookTitle").value("1984"));
        }

        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return 404 Not Found for non-existent loan ID")
        void shouldReturn404WhenLoanNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/loans/999").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO library_user (id, username, password, role) VALUES (3, 'otheruser', '{noop}user123', 'USER');
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 3, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return 403 Forbidden when viewing another user's loan")
        void shouldReturn403WhenViewingOtherUsersLoan() throws Exception {
            mockMvc.perform(get("/api/v1/loans/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/loans")
    class GetLoansTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO book (id, title, author_id) VALUES (2, 'The Lord of the Rings', 2);
                        INSERT INTO library_user (id, username, password, role) VALUES (3, 'otheruser', '{noop}user123', 'USER');
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 1, '2026-05-11', 1);
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (2, 3, '2026-05-10', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "user")
        @DisplayName("should only return loans for the authenticated user")
        void shouldReturnOnlyOwnLoans() throws Exception {
            mockMvc.perform(get("/api/v1/loans").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].username").value("user"))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell'), (2, 'J.R.R. Tolkien');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO book (id, title, author_id) VALUES (2, 'The Lord of the Rings', 2);
                        INSERT INTO library_user (id, username, password, role) VALUES (3, 'otheruser', '{noop}user123', 'USER');
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 1, '2026-05-11', 1);
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (2, 3, '2026-05-10', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("should return all loans for admin")
        void shouldReturnAllLoansForAdmin() throws Exception {
            mockMvc.perform(get("/api/v1/loans").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[*].username",
                            hasItems("user", "otheruser")))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @WithMockUser(username = "user")
        @DisplayName("should return empty page when user has no loans")
        void shouldReturnEmptyPageWhenNoLoans() throws Exception {
            mockMvc.perform(get("/api/v1/loans").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }
    }

    @Nested
    @DisplayName("isAvailable on book reflects loan status")
    class BookAvailabilityTests {

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should show book as available when not loaned")
        void shouldShowAvailableWhenNotLoaned() throws Exception {
            mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isAvailable").value(true));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, user_id, loan_date, book_id) VALUES (1, 1, '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should show book as unavailable when loaned")
        void shouldShowUnavailableWhenLoaned() throws Exception {
            mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isAvailable").value(false));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, user_id, loan_date, returned_date, book_id) VALUES (1, 1, '2026-05-11', '2026-05-18', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should show book as available after being returned")
        void shouldShowAvailableAfterReturn() throws Exception {
            mockMvc.perform(get("/api/v1/books/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isAvailable").value(true));
        }
    }

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTests {

        @Autowired private LoanService loanService;
        @Autowired private PlatformTransactionManager transactionManager;

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should only allow one concurrent loan for the same book")
        void shouldOnlyAllowOneConcurrentLoan() throws Exception {
            int threadCount = 10;
            var latch = new CountDownLatch(threadCount);
            var successCount = new AtomicInteger(0);
            var errors = new ArrayList<Throwable>();

            var tt = new TransactionTemplate(transactionManager);

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        latch.countDown();
                        latch.await();
                        tt.executeWithoutResult(status -> {
                            loanService.createLoan("user", 1L);
                            successCount.incrementAndGet();
                        });
                    } catch (BookNotAvailableException
                            | org.springframework.transaction.UnexpectedRollbackException e) {
                        // expected for all but one thread
                    } catch (Throwable t) {
                        synchronized (errors) {
                            errors.add(t);
                        }
                    }
                });
            }

            Thread.sleep(5000);

            assertTrue(errors.isEmpty(), "Unexpected errors: " + errors);
            assertEquals(1, successCount.get(),
                    "Only one loan should have been created");
        }
    }
}
