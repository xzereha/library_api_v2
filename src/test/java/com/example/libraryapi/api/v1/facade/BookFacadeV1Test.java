package com.example.libraryapi.api.v1.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.libraryapi.exception.BookNotFoundException;
import com.example.libraryapi.model.Author;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.service.BookService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** Unit tests for {@link BookFacadeV1}. */
@ExtendWith(MockitoExtension.class)
class BookFacadeV1Test {
    @Mock private BookService bookService;

    private BookFacadeV1 facade;

    @BeforeEach
    void setUp() {
        facade = new BookFacadeV1(bookService);
    }

    @Test
    void createBookWithNullRequestThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> facade.createBook(null));
    }

    @Test
    void deleteBookWhenBookExistsCallsServiceDelete() {
        var author = new Author("Test Author", null);
        when(bookService.getBookById(1L))
                .thenReturn(Optional.of(new Book("Existing Book", null, author)));
        facade.deleteBook(1L);
        verify(bookService).deleteBook(1L);
    }

    @Test
    void deleteBookWhenBookNotFoundThrowsBookNotFoundException() {
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> facade.deleteBook(1L));
        verify(bookService, never()).deleteBook(anyLong());
    }

    @Test
    void getBooksWithNoFiltersDelegatesToGetAll() {
        var author = new Author("Test Author", null);
        var books = List.of(new Book("Book A", null, author));
        when(bookService.getAll(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(books));
        var result = facade.getBooks(null, null, null, Pageable.unpaged());
        assertEquals(1, result.data().size());
        assertEquals("Book A", result.data().getFirst().title());
        verify(bookService).getAll(Pageable.unpaged());
    }

    @Test
    void getBooksWithIsbnFilterDelegatesToSearchBooks() {
        var author = new Author("Test Author", null);
        var books = List.of(new Book("Book A", "9780141037145", author));
        var pageable = Pageable.unpaged();
        when(bookService.searchBooks(eq("9780141037145"), isNull(), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(books));
        var result = facade.getBooks("9780141037145", null, null, pageable);
        assertEquals(1, result.data().size());
    }

    @Test
    void getBooksWithTitleFilterDelegatesToSearchBooks() {
        var author = new Author("Test Author", null);
        var books = List.of(new Book("Lord of the Rings", null, author));
        var pageable = Pageable.unpaged();
        when(bookService.searchBooks(isNull(), eq("lord"), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(books));
        var result = facade.getBooks(null, "lord", null, pageable);
        assertEquals(1, result.data().size());
    }

    @Test
    void getBooksWithAuthorNameFilterDelegatesToSearchBooks() {
        var author = new Author("J.R.R. Tolkien", null);
        var books = List.of(new Book("Lord of the Rings", null, author));
        var pageable = Pageable.unpaged();
        when(bookService.searchBooks(isNull(), isNull(), eq("tolkien"), eq(pageable)))
                .thenReturn(new PageImpl<>(books));
        var result = facade.getBooks(null, null, "tolkien", pageable);
        assertEquals(1, result.data().size());
    }

    @Test
    void getBooksWithAllFiltersDelegatesToSearchBooks() {
        var author = new Author("J.R.R. Tolkien", null);
        var books = List.of(new Book("Lord of the Rings", "9780544003415", author));
        var pageable = Pageable.unpaged();
        when(bookService.searchBooks(
                eq("9780544003415"), eq("lord"), eq("tolkien"), eq(pageable)))
                .thenReturn(new PageImpl<>(books));
        var result = facade.getBooks("9780544003415", "lord", "tolkien", pageable);
        assertEquals(1, result.data().size());
    }
}
