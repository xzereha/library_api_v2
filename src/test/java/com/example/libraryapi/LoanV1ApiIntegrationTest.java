package com.example.libraryapi;

import static org.hamcrest.Matchers.hasItems;
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
                DELETE FROM book;
                DELETE FROM author;
                ALTER TABLE loan ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE book ALTER COLUMN id RESTART WITH 1;
                ALTER TABLE author ALTER COLUMN id RESTART WITH 1;
                """,
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
public class LoanV1ApiIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Nested
    @DisplayName("POST /api/v1/loans")
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
                        "personName": "John Doe",
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
                    .andExpect(jsonPath("$.data.personName").value("John Doe"))
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
                        "personName": "John Doe",
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
                        "personName": "John Doe",
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
                        INSERT INTO loan (id, person_name, loan_date, book_id) VALUES (1, 'John Doe', '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return a book and set returned date")
        void shouldReturnBook() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/1/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.personName").value("John Doe"))
                    .andExpect(jsonPath("$.data.loanDate").value("2026-05-11"))
                    .andExpect(jsonPath("$.data.returnedDate").value("2026-05-11"))
                    .andExpect(jsonPath("$.data.bookId").value(1));
        }

        @Sql(
                statements =
                        """
                        INSERT INTO author (id, name) VALUES (1, 'George Orwell');
                        INSERT INTO book (id, title, author_id) VALUES (1, '1984', 1);
                        INSERT INTO loan (id, person_name, loan_date, returned_date, book_id) VALUES (1, 'John Doe', '2026-05-11', '2026-05-18', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
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
        @DisplayName("should return 404 Not Found when loan does not exist")
        void shouldReturn404WhenLoanNotFound() throws Exception {
            mockMvc.perform(
                            patch("/api/v1/loans/999/return")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
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
                        INSERT INTO loan (id, person_name, loan_date, book_id) VALUES (1, 'John Doe', '2026-05-11', 1);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return loan details for a valid ID")
        void shouldReturnLoanById() throws Exception {
            mockMvc.perform(get("/api/v1/loans/1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.personName").value("John Doe"))
                    .andExpect(jsonPath("$.data.bookTitle").value("1984"));
        }

        @Test
        @DisplayName("should return 404 Not Found for non-existent loan ID")
        void shouldReturn404WhenLoanNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/loans/999").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
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
                        INSERT INTO loan (id, person_name, loan_date, book_id) VALUES (1, 'John Doe', '2026-05-11', 1);
                        INSERT INTO loan (id, person_name, loan_date, book_id) VALUES (2, 'Jane Smith', '2026-05-10', 2);
                        """,
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
        @Test
        @DisplayName("should return a paginated list of loans")
        void shouldReturnPaginatedLoans() throws Exception {
            mockMvc.perform(get("/api/v1/loans").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.version").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[*].personName",
                            hasItems("John Doe", "Jane Smith")))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        @DisplayName("should return empty page when no loans exist")
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
                        INSERT INTO loan (id, person_name, loan_date, book_id) VALUES (1, 'John Doe', '2026-05-11', 1);
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
                        INSERT INTO loan (id, person_name, loan_date, returned_date, book_id) VALUES (1, 'John Doe', '2026-05-11', '2026-05-18', 1);
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
                int threadId = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        latch.countDown();
                        latch.await();
                        tt.executeWithoutResult(status -> {
                            loanService.createLoan("Thread-" + threadId, 1L);
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
