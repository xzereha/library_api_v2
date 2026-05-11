package com.example.libraryapi.api.v1.facade;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.libraryapi.service.LoanService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link LoanFacadeV1}. */
@ExtendWith(MockitoExtension.class)
class LoanFacadeV1Test {
    @Mock private LoanService loanService;

    private LoanFacadeV1 facade;

    @BeforeEach
    void setUp() {
        facade = new LoanFacadeV1(loanService);
    }

    @Test
    void createLoanWithNullRequestThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> facade.createLoan(null));
    }
}
