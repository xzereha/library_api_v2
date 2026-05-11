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

import java.time.LocalDate;
import java.util.Optional;

/** Loan Entity. Tracks when a book is borrowed and returned. */
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate loanDate;

    @Nullable
    @Column(nullable = true)
    private LocalDate returnedDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** Default constructor for JPA. Should not be used directly. */
    protected Loan() {}

    /**
     * Constructor for a new Loan.
     *
     * @param user         The user borrowing the book
     * @param loanDate     Date the book was loaned
     * @param returnedDate Date the book was returned, or null if not yet returned
     * @param book         The book being loaned
     */
    public Loan(
            User user,
            LocalDate loanDate,
            @Nullable LocalDate returnedDate,
            Book book) {
        setUser(user);
        setLoanDate(loanDate);
        setReturnedDate(returnedDate);
        setBook(book);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }

    /**
     * Sets the user for this loan.
     *
     * @param user The user borrowing the book.
     */
    public void setUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        this.user = user;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    /**
     * Update the loan date.
     *
     * @param loanDate New loan date, must not be null.
     */
    public void setLoanDate(LocalDate loanDate) {
        if (loanDate == null) {
            throw new IllegalArgumentException("Loan date must not be null");
        }
        this.loanDate = loanDate;
    }

    public Optional<LocalDate> getReturnedDate() {
        return Optional.ofNullable(returnedDate);
    }

    /**
     * Set the returned date, or null to mark as not yet returned.
     *
     * @param returnedDate Date the book was returned, or null.
     */
    public void setReturnedDate(@Nullable LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public Book getBook() {
        return book;
    }

    /**
     * Sets the book for this loan.
     *
     * @param book The book being loaned.
     */
    public void setBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }
        this.book = book;
    }
}
