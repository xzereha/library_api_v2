package com.example.libraryapi.api.v1.facade;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.Response;
import com.example.libraryapi.api.v1.dto.BookRequestV1;
import com.example.libraryapi.api.v1.dto.BookResponseV1;
import com.example.libraryapi.exception.BookNotFoundException;
import com.example.libraryapi.service.BookService;

import jakarta.annotation.Nullable;

import lombok.NonNull;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade for {@link BookService BookService}.
 *
 * <p>This facade is used for mapping between the versioned DTOs and the service layer.
 */
@Service
public class BookFacadeV1 {
    static final int VERSION = 1;
    private final BookService bookService;

    /**
     * Constructor.
     *
     * @param bookService the service to use for managing books
     */
    public BookFacadeV1(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get all books with pagination and optional filters.
     *
     * @param isbn optional exact ISBN match, null to ignore
     * @param title optional case-insensitive partial title match, null to ignore
     * @param authorName optional case-insensitive partial author name match, null to ignore
     * @param pageable pagination information
     * @return a paginated response of books
     */
    public PagedResponse<BookResponseV1> getBooks(
            @Nullable String isbn,
            @Nullable String title,
            @Nullable String authorName,
            Pageable pageable) {
        boolean hasFilters = isbn != null || title != null || authorName != null;
        var page = hasFilters
                ? bookService.searchBooks(isbn, title, authorName, pageable)
                : bookService.getAll(pageable);
        var books = page.getContent().stream().map(BookResponseV1::fromBook).toList();
        return new PagedResponse<>(
                books,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                VERSION);
    }

    /**
     * Get a book by ID.
     *
     * @param id Id to retrieve
     * @return The found book.
     * @throws BookNotFoundException if no book with the given ID exists
     */
    public BookResponseV1 getBook(Long id) {
        return bookService
                .getBookById(id)
                .map(BookResponseV1::fromBook)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    /**
     * Create a book.
     *
     * @param request the request DTO containing the data for creating the book
     * @return the response DTO containing the created book
     */
    public BookResponseV1 createBook(@NonNull BookRequestV1 request) {
        var book =
                bookService.createBook(
                        request.title(), Optional.ofNullable(request.isbn()), request.authorId());
        return BookResponseV1.fromBook(book);
    }

    /**
     * Get all books by a given author ID.
     *
     * @param authorId the ID of the author
     * @param pageable pagination information
     * @return a paginated response of books by the author
     */
    public PagedResponse<BookResponseV1> getBooksByAuthorId(long authorId, Pageable pageable) {
        var page = bookService.getBooksByAuthorId(authorId, pageable);
        var books = page.getContent().stream().map(BookResponseV1::fromBook).toList();
        return new PagedResponse<>(
                books,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                VERSION);
    }

    /**
     * Delete a book by ID.
     *
     * @param id the ID of the book to delete
     * @throws BookNotFoundException if no book with the given ID exists
     */
    public void deleteBook(long id) {
        bookService
                .getBookById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        bookService.deleteBook(id);
    }
}
