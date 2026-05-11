package com.example.libraryapi.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

/** Request DTO for creating a {@link com.example.libraryapi.model.Loan Loan}. */
@Schema(description = "Request payload for borrowing a book.")
@Validated
public record LoanRequestV1(
        @NotBlank(message = "Person name is required.")
                @Schema(
                        description = "Name of the person borrowing the book",
                        example = "John Doe",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String personName,
        @Min(value = 1, message = "Book ID must be a positive number.")
                @Schema(
                        description = "ID of the book to borrow",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long bookId) {}
