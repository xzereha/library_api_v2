package com.example.libraryapi.exception;

/** Exception thrown when a loan with the specified ID is not found. */
public class LoanNotFoundException extends RuntimeException {
    /**
     * Constructor for LoanNotFoundException.
     *
     * @param id The ID of the loan that was not found
     */
    public LoanNotFoundException(Long id) {
        super("Loan with ID: " + id + " not found");
    }
}
