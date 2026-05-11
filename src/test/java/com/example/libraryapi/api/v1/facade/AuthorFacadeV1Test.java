package com.example.libraryapi.api.v1.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
