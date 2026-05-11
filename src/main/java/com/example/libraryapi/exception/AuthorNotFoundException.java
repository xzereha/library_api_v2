package com.example.libraryapi.exception;

/** Exception thrown when an author with the specified ID is not found. */
public class AuthorNotFoundException extends RuntimeException {
    /**
     * Constructor for AuthorNotFoundException.
     *
     * @param id The ID of the author that was not found
     */
    public AuthorNotFoundException(Long id) {
        super("Author with ID: " + id + " not found");
    }
}
