package com.example.libraryapi.repository;

import com.example.libraryapi.model.Loan;
import com.example.libraryapi.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Database repository for {@link Loan Loan}. */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Find an active loan for a given book (one that has not been returned).
     *
     * @param bookId the ID of the book
     * @return an {@link Optional} containing the active loan or empty if none exists
     */
    Optional<Loan> findByBookIdAndReturnedDateIsNull(Long bookId);

    /**
     * Check if a book has an active loan.
     *
     * @param bookId the ID of the book
     * @return true if there is an active (unreturned) loan for the book
     */
    boolean existsByBookIdAndReturnedDateIsNull(Long bookId);

    /**
     * Find all loans for a given user, with pagination support.
     *
     * @param user     the user whose loans to retrieve
     * @param pageable pagination information
     * @return a paginated list of loans for the user
     */
    Page<Loan> findByUser(User user, Pageable pageable);
}
