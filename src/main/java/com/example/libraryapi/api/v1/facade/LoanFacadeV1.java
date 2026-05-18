package com.example.libraryapi.api.v1.facade;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.v1.dto.LoanRequestV1;
import com.example.libraryapi.api.v1.dto.LoanResponseV1;
import com.example.libraryapi.exception.LoanNotFoundException;
import com.example.libraryapi.service.LoanService;

import lombok.NonNull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Facade for {@link LoanService LoanService}. */
@Service
public class LoanFacadeV1 {
    static final int VERSION = 1;
    private final LoanService loanService;

    /**
     * Constructor.
     *
     * @param loanService the service to use for managing loans
     */
    public LoanFacadeV1(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Borrow a book. The username is taken from the authenticated user.
     *
     * @param request the request DTO containing the book ID
     * @return the response DTO containing the created loan
     */
    @CacheEvict(value = "loans", allEntries = true)
    public LoanResponseV1 createLoan(@NonNull LoanRequestV1 request) {
        var username = currentUsername();
        var loan = loanService.createLoan(username, request.bookId());
        return LoanResponseV1.fromLoan(loan);
    }

    /**
     * Return a book. Only the loan owner or an admin may return a loan.
     *
     * @param id the ID of the loan to return
     * @return the response DTO containing the updated loan
     * @throws LoanNotFoundException if no loan with the given ID exists
     * @throws AccessDeniedException if the user is not the loan owner and is not an admin
     */
    @CacheEvict(value = "loans", allEntries = true)
    public LoanResponseV1 returnBook(long id) {
        var loan = loanService
                .getLoanById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
        requireSelfOrAdmin(loan.getUsername());
        var updatedLoan = loanService.returnBook(id);
        return LoanResponseV1.fromLoan(updatedLoan);
    }

    /**
     * Get a loan by ID. Only the loan owner or an admin may view it.
     *
     * @param id Id to retrieve
     * @return The found loan.
     * @throws LoanNotFoundException if no loan with the given ID exists
     * @throws AccessDeniedException if the user is not the loan owner and is not an admin
     */
    @Cacheable(value = "loans", key = "#id")
    public LoanResponseV1 getLoan(Long id) {
        var loan = loanService
                .getLoanById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));
        requireSelfOrAdmin(loan.getUsername());
        return LoanResponseV1.fromLoan(loan);
    }

    /**
     * Get all loans with pagination. Regular users only see their own loans; admins see all.
     *
     * @param pageable pagination information
     * @return a paginated response of loans
     */
    @Cacheable(value = "loans", key = "#pageable")
    public PagedResponse<LoanResponseV1> getLoans(Pageable pageable) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        var page = isAdmin
                ? loanService.getAll(pageable)
                : loanService.getLoansByUsername(auth.getName(), pageable);
        var loans = page.getContent().stream().map(LoanResponseV1::fromLoan).toList();
        return new PagedResponse<>(
                loans,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                VERSION);
    }

    private static String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private static void requireSelfOrAdmin(String targetUsername) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getName().equals(targetUsername)) {
            return;
        }
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }
        throw new AccessDeniedException(
                "Access denied: you are not '" + targetUsername + "' and do not have ADMIN role");
    }
}
