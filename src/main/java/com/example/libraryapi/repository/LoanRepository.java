package com.example.libraryapi.repository;

import com.example.libraryapi.model.Loan;

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
}
