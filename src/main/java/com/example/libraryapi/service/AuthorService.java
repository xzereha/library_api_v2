package com.example.libraryapi.service;

import com.example.libraryapi.model.Author;
import com.example.libraryapi.repository.AuthorRepository;

import jakarta.validation.constraints.NotBlank;

import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/** Service layer for managing {@link com.example.libraryapi.model.Author Authors}. */
@Service
@Validated
public class AuthorService {
    private final AuthorRepository authorRepository;

    /**
     * Constructor for Spring dependency injection.
     *
     * @param authorRepository Repository for managing authors
     */
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /**
     * Create a new {@link com.example.libraryapi.model.Author Author}.
     *
     * @param name the name of the author
     * @param isni the ISNI of the author or none if not available
     * @return the created author
     */
    public Author createAuthor(@NotBlank String name, Optional<String> isni) {
        var author = new Author(name, isni.orElse(null));
        return authorRepository.save(author);
    }

    /**
     * Get an {@link com.example.libraryapi.model.Author Author} by its ID.
     *
     * @param id the ID of the author to get
     * @return an {@link Optional} containing the found author or empty if not found
     */
    public Optional<Author> getAuthorById(long id) {
        return authorRepository.findById(id);
    }

    /**
     * Get an {@link com.example.libraryapi.model.Author Author} by its ISNI.
     *
     * @param isni the ISNI of the author.
     * @return The Author if found, otherwise an empty Optional.
     */
    public Optional<Author> getAuthorByIsni(@NonNull String isni) {
        return authorRepository.findByIsni(isni);
    }

    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    /**
     * Deletes the {@link com.example.libraryapi.model.Author Author} with the given ID.
     *
     * @param id the ID of the author to be deleted
     */
    public void deleteAuthor(long id) {
        authorRepository.deleteById(id);
    }
}
