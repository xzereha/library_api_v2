package com.example.libraryapi.exception;

/** Exception thrown when an author with the specified identifier is not found. */
public class AuthorNotFoundException extends RuntimeException {
    /**
     * Constructor for AuthorNotFoundException using an ID.
     *
     * @param id The ID of the author that was not found
     */
    public AuthorNotFoundException(Long id) {
        super("Author with ID: " + id + " not found");
    }
}
