package com.example.libraryapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.libraryapi.model.Author;

import org.junit.jupiter.api.Test;

import java.util.Optional;

class AuthorTest {
    @Test
    void testConstructorValidNameAndValidIsniConstructs() {
        String name = "Valid Name";
        String isni = "1000000000001234";
        Author author = new Author(name, isni);
        assertNotNull(author);
        assertEquals(author.getName(), name);
        assertEquals(author.getIsni(), Optional.of(isni));
    }

    @Test
    void testConstructorBlankNameThrows() {
        String name = "";
        String isni = "1000000000001234";
        assertThrows(IllegalArgumentException.class, () -> new Author(name, isni));
    }

    @Test
    void testConstructorNullNameThrows() {
        String name = null;
        String isni = "1000000000001234";
        assertThrows(IllegalArgumentException.class, () -> new Author(name, isni));
    }

    @Test
    void testConstructorValidNameAndNullIsniConstructs() {
        String name = "Valid Name";
        String isni = null;
        Author author = new Author(name, isni);
        assertNotNull(author);
        assertEquals(author.getName(), name);
        assertEquals(author.getIsni(), Optional.empty());
    }

    @Test
    void testConstructorValidNameAndInvalidIsniThrows() {
        String name = "Valid Name";
        String isni = "Invalid";
        assertThrows(IllegalArgumentException.class, () -> new Author(name, isni));
    }
}
