package com.example.libraryapi.api.v1.facade;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.v1.dto.LoanRequestV1;
import com.example.libraryapi.api.v1.dto.LoanResponseV1;
import com.example.libraryapi.exception.LoanNotFoundException;
import com.example.libraryapi.service.LoanService;

import lombok.NonNull;

import org.springframework.data.domain.Pageable;
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
     * Borrow a book.
     *
     * @param request the request DTO containing person name and book ID
     * @return the response DTO containing the created loan
     */
    public LoanResponseV1 createLoan(@NonNull LoanRequestV1 request) {
        var loan = loanService.createLoan(request.personName(), request.bookId());
        return LoanResponseV1.fromLoan(loan);
    }

    /**
     * Return a book.
     *
     * @param id the ID of the loan to return
     * @return the response DTO containing the updated loan
     * @throws LoanNotFoundException if no loan with the given ID exists
     */
    public LoanResponseV1 returnBook(long id) {
        var loan = loanService.returnBook(id);
        return LoanResponseV1.fromLoan(loan);
    }

    /**
     * Get a loan by ID.
     *
     * @param id Id to retrieve
     * @return The found loan.
     * @throws LoanNotFoundException if no loan with the given ID exists
     */
    public LoanResponseV1 getLoan(Long id) {
        return loanService
                .getLoanById(id)
                .map(LoanResponseV1::fromLoan)
                .orElseThrow(() -> new LoanNotFoundException(id));
    }

    /**
     * Get all loans with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of loans
     */
    public PagedResponse<LoanResponseV1> getLoans(Pageable pageable) {
        var page = loanService.getAll(pageable);
        var loans = page.getContent().stream().map(LoanResponseV1::fromLoan).toList();
        return new PagedResponse<>(
                loans,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                VERSION);
    }
}
