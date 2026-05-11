package com.example.libraryapi.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/** Book Entity. */
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Size(min = 13, max = 13, message = "ISBN must be exactly 13 characters")
    @Nullable
    @Column(unique = true, nullable = true)
    private String isbn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    /** Default constructor for JPA. Should not be used directly */
    protected Book() {}

    /**
     * Constructor for a new Book.
     *
     * @param title Title of the book
     * @param isbn ISBN-13 of the book or null
     * @param author Author of the book
     */
    public Book(@NotBlank String title, @Nullable String isbn, Author author) {
        setTitle(title);
        setIsbn(isbn);
        setAuthor(author);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Update the title of the Book.
     *
     * @param title New title of the book, must not be blank.
     */
    public void setTitle(@NotBlank String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        this.title = title;
    }

    public Optional<String> getIsbn() {
        return Optional.ofNullable(isbn);
    }

    /**
     * Sets the ISBN-13 number of the Book.
     *
     * <p>The format must be exactly 13 characters. Hyphens and spaces between them are accepted.
     *
     * <p>Example: <i>978-0-618-05682-4</i>
     *
     * <p>If null is passed in then the ISBN is removed.
     *
     * @param isbn ISBN-13 for the Book, or null to clear.
     */
    public void setIsbn(@Nullable String isbn) {
        if (isbn == null) {
            this.isbn = null;
            return;
        }
        String compactedIsbn = isbn.replaceAll("[\\s-]", "");
        if (compactedIsbn.length() != 13) {
            throw new IllegalArgumentException("ISBN must be exactly 13 characters long.");
        }
        this.isbn = compactedIsbn;
    }

    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the author of the Book, or null to remove the relationship (e.g. for orphan removal).
     *
     * @param author Author of the book, or null to clear.
     */
    public void setAuthor(Author author) {
        this.author = author;
    }
}
