package com.example.libraryapi.exception;

/** Exception thrown when a book with the specified ID is not found. */
public class BookNotFoundException extends RuntimeException {
    /**
     * Constructor for BookNotFoundException.
     *
     * @param id The ID of the book that was not found
     */
    public BookNotFoundException(Long id) {
        super("Book with ID: " + id + " not found");
    }
}
