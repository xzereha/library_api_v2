package com.example.libraryapi.api.v1.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.libraryapi.exception.AuthorNotFoundException;
import com.example.libraryapi.model.Author;
import com.example.libraryapi.service.AuthorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** Unit tests for {@link AuthorFacadeV1}. */
@ExtendWith(MockitoExtension.class)
class AuthorFacadeV1Test {
    @Mock private AuthorService authorService;

    private AuthorFacadeV1 facade;

    @BeforeEach
    void setUp() {
        facade = new AuthorFacadeV1(authorService);
    }

    @Test
    void createAuthorWithNullRequestThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> facade.createAuthor(null));
    }

    @Test
    void deleteAuthorWhenAuthorExistsCallsServiceDelete() {
        when(authorService.getAuthorById(1L))
                .thenReturn(Optional.of(new Author("Existing Author", null)));
        facade.deleteAuthor(1L);
        verify(authorService).deleteAuthor(1L);
    }

    @Test
    void deleteAuthorWhenAuthorNotFoundThrowsAuthorNotFoundException() {
        when(authorService.getAuthorById(1L)).thenReturn(Optional.empty());
        assertThrows(AuthorNotFoundException.class, () -> facade.deleteAuthor(1L));
        verify(authorService, never()).deleteAuthor(anyLong());
    }

    @Test
    void getAuthorsWithIsniWhenFoundReturnsPageWithSingleAuthor() {
        var author = new Author("George Orwell", "0000000121351230");
        when(authorService.getAuthorByIsni("0000000121351230"))
                .thenReturn(Optional.of(author));
        var result = facade.getAuthors("0000000121351230", Pageable.unpaged());
        assertEquals(1, result.data().size());
        assertEquals("George Orwell", result.data().getFirst().name());
        assertEquals("0000000121351230", result.data().getFirst().isni());
        assertEquals(1, result.totalElements());
    }

    @Test
    void getAuthorsWithIsniWhenNotFoundReturnsEmptyPage() {
        when(authorService.getAuthorByIsni("0000000000000000"))
                .thenReturn(Optional.empty());
        var result = facade.getAuthors("0000000000000000", Pageable.unpaged());
        assertTrue(result.data().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void getAuthorsWithNullIsniReturnsAllAuthors() {
        var authors = List.of(
                new Author("George Orwell", null),
                new Author("Aldous Huxley", null));
        when(authorService.getAll(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(authors));
        var result = facade.getAuthors(null, Pageable.unpaged());
        assertEquals(2, result.data().size());
    }
}
