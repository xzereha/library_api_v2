package com.example.libraryapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.libraryapi.model.Author;
import com.example.libraryapi.model.Book;
import com.example.libraryapi.model.Loan;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class LoanTest {

    private final Author author = new Author("George Orwell", null);
    private final Book book = new Book("1984", null, author);

    @Test
    void testConstructorValidFieldsConstructs() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var loan = new Loan("John Doe", loanDate, null, book);
        assertNotNull(loan);
        assertEquals("John Doe", loan.getPersonName());
        assertEquals(loanDate, loan.getLoanDate());
        assertTrue(loan.getReturnedDate().isEmpty());
        assertEquals(book, loan.getBook());
    }

    @Test
    void testConstructorNullPersonNameThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Loan(null, loanDate, null, book));
    }

    @Test
    void testConstructorBlankPersonNameThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Loan("", loanDate, null, book));
    }

    @Test
    void testConstructorNullLoanDateThrows() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Loan("John Doe", null, null, book));
    }

    @Test
    void testConstructorNullBookThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Loan("John Doe", loanDate, null, null));
    }

    @Test
    void testSetReturnedDate() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var loan = new Loan("John Doe", loanDate, null, book);
        assertTrue(loan.getReturnedDate().isEmpty());
        var returnedDate = LocalDate.of(2026, 5, 18);
        loan.setReturnedDate(returnedDate);
        assertEquals(returnedDate, loan.getReturnedDate().orElseThrow());
    }

    @Test
    void testConstructorWithReturnedDate() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var returnedDate = LocalDate.of(2026, 5, 18);
        var loan = new Loan("John Doe", loanDate, returnedDate, book);
        assertEquals(returnedDate, loan.getReturnedDate().orElseThrow());
    }

    @Test
    void testSetPersonNameBlankThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var loan = new Loan("John Doe", loanDate, null, book);
        assertThrows(IllegalArgumentException.class, () -> loan.setPersonName(""));
    }

    @Test
    void testSetLoanDateNullThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var loan = new Loan("John Doe", loanDate, null, book);
        assertThrows(IllegalArgumentException.class, () -> loan.setLoanDate(null));
    }

    @Test
    void testSetBookNullThrows() {
        var loanDate = LocalDate.of(2026, 5, 11);
        var loan = new Loan("John Doe", loanDate, null, book);
        assertThrows(IllegalArgumentException.class, () -> loan.setBook(null));
    }
}
