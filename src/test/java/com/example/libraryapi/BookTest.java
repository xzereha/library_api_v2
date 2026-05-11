package com.example.libraryapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.libraryapi.model.Author;
import com.example.libraryapi.model.Book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class BookTest {
    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author("Valid Author", null);
    }

    @Test
    void testConstructorValidTitleValidIsbnAndValidAuthorConstructs() {
        String title = "Valid Title";
        String isbn = "9780000000001";
        Book book = new Book(title, isbn, author);
        assertNotNull(book);
        assertEquals(title, book.getTitle());
        assertEquals(Optional.of(isbn), book.getIsbn());
        assertEquals(author, book.getAuthor());
    }

    @Test
    void testConstructorValidTitleNullIsbnConstructs() {
        Book book = new Book("Valid Title", null, author);
        assertNotNull(book);
        assertEquals(Optional.empty(), book.getIsbn());
    }

    @Test
    void testConstructorBlankTitleThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Book("", null, author));
    }

    @Test
    void testConstructorNullTitleThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Book(null, null, author));
    }

    @Test
    void testConstructorInvalidIsbnTooShortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Book("Title", "123", author));
    }

    @Test
    void testConstructorInvalidIsbnTooLongThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Book("Title", "978000000000123", author));
    }

    @Test
    void testConstructorIsbnWithHyphensIsAccepted() {
        String rawIsbn = "978-0-618-05682-4";
        String compacted = "9780618056824";
        Book book = new Book("Title", rawIsbn, author);
        assertEquals(Optional.of(compacted), book.getIsbn());
    }

    @Test
    void testConstructorIsbnWithSpacesIsAccepted() {
        String rawIsbn = "978 0 618 05682 4";
        String compacted = "9780618056824";
        Book book = new Book("Title", rawIsbn, author);
        assertEquals(Optional.of(compacted), book.getIsbn());
    }

    @Test
    void testConstructorNullAuthorDoesNotThrow() {
        Book book = new Book("Title", null, null);
        assertNotNull(book);
    }

    @Test
    void testSetBlankTitleThrows() {
        Book book = new Book("Title", null, author);
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(""));
    }

    @Test
    void testSetNullTitleThrows() {
        Book book = new Book("Title", null, author);
        assertThrows(IllegalArgumentException.class, () -> book.setTitle(null));
    }

    @Test
    void testSetNullIsbnClearsIsbn() {
        Book book = new Book("Title", "9780000000001", author);
        book.setIsbn(null);
        assertEquals(Optional.empty(), book.getIsbn());
    }

    @Test
    void testSetInvalidIsbnThrows() {
        Book book = new Book("Title", null, author);
        assertThrows(IllegalArgumentException.class, () -> book.setIsbn("invalid"));
    }

    @Test
    void testSetNullAuthorIsAllowed() {
        Book book = new Book("Title", null, author);
        book.setAuthor(null);
    }

    @Test
    void testAuthorRelationshipViaAuthorAddBook() {
        author.addBook(new Book("Book One", null, author));
        author.addBook(new Book("Book Two", null, author));
        assertEquals(2, author.getBooks().size());
        assertEquals("Book One", author.getBooks().getFirst().getTitle());
    }

    @Test
    void testAuthorRelationshipViaAuthorRemoveBook() {
        Book book = new Book("Book One", null, author);
        author.addBook(book);
        assertEquals(1, author.getBooks().size());
        author.removeBook(book);
        assertEquals(0, author.getBooks().size());
        assertTrue(author.getBooks().isEmpty());
    }

    @Test
    void testAuthorBooksIsUnmodifiable() {
        author.addBook(new Book("Book One", null, author));
        assertThrows(
                UnsupportedOperationException.class,
                () -> author.getBooks().add(new Book("Another", null, author)));
    }
}
