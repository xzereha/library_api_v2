package com.example.libraryapi.service;

import com.example.libraryapi.exception.BookNotAvailableException;
import com.example.libraryapi.exception.BookNotFoundException;
import com.example.libraryapi.exception.LoanNotFoundException;
import com.example.libraryapi.model.Loan;
import com.example.libraryapi.repository.BookRepository;
import com.example.libraryapi.repository.LoanRepository;
import com.example.libraryapi.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;

import lombok.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Optional;

/** Service layer for managing {@link Loan Loans}. */
@Service
@Validated
public class LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Constructor for Spring dependency injection.
     *
     * @param loanRepository Repository for managing loans
     * @param bookRepository Repository for managing books
     * @param userRepository Repository for user data
     */
    public LoanService(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new loan (borrow a book).
     *
     * <p>This method is transactional and uses a pessimistic write lock on the book row to
     * prevent race conditions where two concurrent requests could both see no active loan
     * and proceed to insert.
     *
     * @param username the username of the person borrowing the book
     * @param bookId the ID of the book to borrow
     * @return the created loan
     * @throws BookNotFoundException if no book with the given ID exists
     * @throws BookNotAvailableException if the book already has an active loan
     */
    @Transactional
    public Loan createLoan(@NotBlank String username, long bookId) {
        var book = bookRepository
                .findByIdWithLock(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        if (loanRepository.existsByBookIdAndReturnedDateIsNull(bookId)) {
            throw new BookNotAvailableException(bookId);
        }
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new IllegalStateException("User not found: " + username));
        var loan = new Loan(user, LocalDate.now(), null, book);
        return loanRepository.save(loan);
    }

    /**
     * Return a book by marking its loan as returned.
     *
     * @param loanId the ID of the loan to return
     * @return the updated loan with the returned date set
     * @throws LoanNotFoundException if no loan with the given ID exists
     */
    public Loan returnBook(long loanId) {
        var loan = loanRepository
                .findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        if (loan.getReturnedDate().isEmpty()) {
            loan.setReturnedDate(LocalDate.now());
            return loanRepository.save(loan);
        }
        return loan;
    }

    /**
     * Get a loan by its ID.
     *
     * @param id the ID of the loan
     * @return an {@link Optional} containing the found loan or empty if not found
     */
    public Optional<Loan> getLoanById(long id) {
        return loanRepository.findById(id);
    }

    /**
     * Get all loans with pagination support.
     *
     * @param pageable pagination information
     * @return a paginated list of loans
     */
    public Page<Loan> getAll(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    /**
     * Get all loans for a specific username, with pagination support.
     *
     * @param username the username to filter by
     * @param pageable pagination information
     * @return a paginated list of loans for the user
     */
    public Page<Loan> getLoansByUsername(String username, Pageable pageable) {
        var user = userRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new IllegalStateException("User not found: " + username));
        return loanRepository.findByUser(user, pageable);
    }

    /**
     * Check if a book has an active (unreturned) loan.
     *
     * @param bookId the ID of the book
     * @return true if the book has an active loan
     */
    public boolean hasActiveLoan(long bookId) {
        return loanRepository.existsByBookIdAndReturnedDateIsNull(bookId);
    }
}
