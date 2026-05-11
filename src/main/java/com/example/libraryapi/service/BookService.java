package com.example.libraryapi.service;

import com.example.libraryapi.exception.AuthorNotFoundException;
import com.example.libraryapi.exception.BookNotFoundException;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.repository.BookRepository;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import lombok.NonNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/** Service layer for managing {@link Book Books}. */
@Service
@Validated
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorService authorService;

    /**
     * Constructor for Spring dependency injection.
     *
     * @param bookRepository Repository for managing books
     * @param authorService Service for managing authors
     */
    public BookService(BookRepository bookRepository, AuthorService authorService) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
    }

    /**
     * Create a new {@link Book Book}.
     *
     * @param title the title of the book
     * @param isbn the ISBN-13 of the book or none if not available
     * @param authorId the ID of the author
     * @return the created book
     * @throws AuthorNotFoundException if no author with the given ID exists
     */
    public Book createBook(@NotBlank String title, Optional<String> isbn, long authorId) {
        var author =
                authorService
                        .getAuthorById(authorId)
                        .orElseThrow(() -> new AuthorNotFoundException(authorId));
        var book = new Book(title, isbn.orElse(null), author);
        return bookRepository.save(book);
    }

    /**
     * Get a {@link Book Book} by its ID.
     *
     * @param id the ID of the book to get
     * @return an {@link Optional} containing the found book or empty if not found
     */
    public Optional<Book> getBookById(long id) {
        return bookRepository.findById(id);
    }

    /**
     * Get all {@link Book Books} with pagination support.
     *
     * @param pageable pagination information
     * @return a paginated list of books
     */
    public Page<Book> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    /**
     * Search books with optional filters.
     *
     * @param isbn exact ISBN match, or null for no filter
     * @param title case-insensitive partial title match, or null for no filter
     * @param authorName case-insensitive partial author name match, or null for no filter
     * @param pageable pagination information
     * @return a filtered paginated list of books
     */
    public Page<Book> searchBooks(
            @Nullable String isbn,
            @Nullable String title,
            @Nullable String authorName,
            Pageable pageable) {
        return bookRepository.searchBooks(isbn, title, authorName, pageable);
    }

    /**
     * Get all {@link Book Books} by a given author ID.
     *
     * @param authorId the ID of the author
     * @param pageable pagination information
     * @return a paginated list of books by the author
     * @throws AuthorNotFoundException if no author with the given ID exists
     */
    public Page<Book> getBooksByAuthorId(long authorId, Pageable pageable) {
        if (authorService.getAuthorById(authorId).isEmpty()) {
            throw new AuthorNotFoundException(authorId);
        }
        return bookRepository.findByAuthorId(authorId, pageable);
    }

    /**
     * Deletes the {@link Book Book} with the given ID.
     *
     * @param id the ID of the book to be deleted
     */
    public void deleteBook(long id) {
        bookRepository.deleteById(id);
    }
}
