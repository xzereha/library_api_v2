package com.example.libraryapi.api.v1.controller;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.Response;
import com.example.libraryapi.api.v1.dto.LoanRequestV1;
import com.example.libraryapi.api.v1.dto.LoanResponseV1;
import com.example.libraryapi.api.v1.facade.LoanFacadeV1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** API controller for {@link com.example.libraryapi.model.Loan Loan}. */
@RestController
@RequestMapping("/api/v" + LoanControllerV1.VERSION)
public class LoanControllerV1 {
    static final int VERSION = 1;
    private final LoanFacadeV1 facade;

    /**
     * Constructor for the controller.
     *
     * @param facade The facade to use for loan operations
     */
    public LoanControllerV1(LoanFacadeV1 facade) {
        this.facade = facade;
    }

    /**
     * Endpoint for borrowing a book. The username is taken from the authenticated user.
     *
     * @param request The request payload containing the book ID.
     * @return The response payload for the created loan.
     */
    @Operation(
            summary = "Borrow a book",
            description = "Creates a new loan for a book, marking it as unavailable."
                    + " The username is derived from the authenticated user.")
    @ApiResponse(
            responseCode = "201",
            description = "Book borrowed successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Book is already loaned out",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "No book with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(
            value = "/loans",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<LoanResponseV1>> createLoan(
            @Valid @RequestBody final LoanRequestV1 request) {
        var response = facade.createLoan(request);
        var location = URI.create("/api/v" + VERSION + "/loans/" + response.id());
        return ResponseEntity.created(location).body(new Response<>(response, VERSION));
    }

    /**
     * Endpoint for returning a book.
     *
     * @param id The ID of the loan to return.
     * @return The response payload for the updated loan.
     */
    @Operation(
            summary = "Return a book",
            description = "Marks a loan as returned by setting the returned date.")
    @ApiResponse(
            responseCode = "200",
            description = "Book returned successfully")
    @ApiResponse(
            responseCode = "404",
            description = "No loan with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PatchMapping(value = "/loans/{id}/return", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<LoanResponseV1>> returnBook(
            @Parameter(description = "ID of the loan to return", example = "1", required = true)
            @PathVariable(name = "id", required = true) final long id) {
        return ResponseEntity.ok(new Response<>(facade.returnBook(id), VERSION));
    }

    /**
     * Endpoint for retrieving a single loan by ID.
     *
     * @param id The ID of the loan to retrieve.
     * @return A ResponseEntity containing a Response object with the retrieved loan.
     */
    @Operation(
            summary = "Get a loan by ID",
            description = "Returns a single loan by its ID.")
    @ApiResponse(
            responseCode = "200",
            description = "The loan was found and returned successfully")
    @ApiResponse(
            responseCode = "404",
            description = "No loan with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping(value = "/loans/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<LoanResponseV1>> getLoanById(
            @Parameter(description = "ID of the loan to retrieve", example = "1", required = true)
            @PathVariable(name = "id", required = true) final long id) {
        return ResponseEntity.ok(new Response<>(facade.getLoan(id), VERSION));
    }

    /**
     * Endpoint for retrieving all loans with pagination support.
     *
     * @param pageable pagination information
     * @return A paginated response of loans
     */
    @Operation(
            summary = "Get all loans",
            description = "Returns a paginated list of all loans.")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of loans retrieved successfully")
    @GetMapping(value = "/loans", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResponse<LoanResponseV1>> getAll(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(facade.getLoans(pageable));
    }
}
