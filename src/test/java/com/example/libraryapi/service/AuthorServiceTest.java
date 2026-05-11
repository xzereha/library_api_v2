package com.example.libraryapi.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.libraryapi.repository.AuthorRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link AuthorService}. */
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
    @Mock private AuthorRepository authorRepository;

    @Test
    void getAuthorByIsniWithNullIsniThrowsNullPointerException() {
        var service = new AuthorService(authorRepository);
        assertThrows(NullPointerException.class, () -> service.getAuthorByIsni(null));
    }
}
