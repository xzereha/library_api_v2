package com.example.libraryapi.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;

/** Request DTO for creating a {@link com.example.libraryapi.model.Loan Loan}. */
@Schema(description = "Request payload for borrowing a book.")
@Validated
public record LoanRequestV1(
        @Min(value = 1, message = "Book ID must be a positive number.")
                @Schema(
                        description = "ID of the book to borrow",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long bookId) {}
