package com.example.libraryapi.config;

import com.example.libraryapi.service.LoanService;

import org.springframework.stereotype.Component;

/**
 * Security bean used in SpEL expressions for loan ownership checks.
 *
 * <p>Methods are referenced from {@code @PreAuthorize} annotations via
 * {@code @loanSecurity.isOwner(#id, authentication.name)}.
 */
@Component("loanSecurity")
public class LoanSecurity {

    private final LoanService loanService;

    /**
     * Constructor.
     *
     * @param loanService the loan service for looking up loans
     */
    public LoanSecurity(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Check if the given username is the owner of the loan with the given ID.
     *
     * <p>If the loan does not exist, returns {@code true} so that the method can throw a
     * {@link com.example.libraryapi.exception.LoanNotFoundException} instead of an access denied
     * error.
     *
     * @param loanId  the ID of the loan
     * @param username the username to check
     * @return true if the loan does not exist or if the username matches the loan's owner
     */
    public boolean isOwner(Long loanId, String username) {
        return loanService
                .getLoanById(loanId)
                .map(loan -> loan.getUser().getUsername().equals(username))
                .orElse(true);
    }
}
