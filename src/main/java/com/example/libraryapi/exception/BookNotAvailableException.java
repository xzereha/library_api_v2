package com.example.libraryapi.exception;

/** Exception thrown when a book is not available for loan. */
public class BookNotAvailableException extends RuntimeException {
    /**
     * Constructor for BookNotAvailableException. This constructor takes the ID of the book that is
     * not available and constructs an error message indicating that the book with the specified ID
     * is not available for loaning. This exception can be thrown when a user attempts to create a
     * loan for a book that is currently loaned out or otherwise unavailable.
     *
     * @param id the unique identifier of the book that is not available for loaning
     */
    public BookNotAvailableException(Long id) {
        super("Book with ID " + id + " is not available.");
    }
}
