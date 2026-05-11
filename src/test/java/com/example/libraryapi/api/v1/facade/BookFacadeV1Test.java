package com.example.libraryapi.api.v1.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
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
}
