package com.example.libraryapi.api.v1.dto;

import com.example.libraryapi.model.Loan;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

/** Response DTO for {@link com.example.libraryapi.model.Loan Loan}. */
@Schema(description = "Response payload for a loan.")
@Validated
public record LoanResponseV1(
        @Nonnull
                @Schema(
                        description = "Unique identifier of the loan",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                Long id,
        @Nonnull
                @Schema(
                        description = "Name of the person who borrowed the book",
                        example = "John Doe",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String personName,
        @Nonnull
                @Schema(
                        description = "Date the book was loaned",
                        example = "2026-05-11",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                LocalDate loanDate,
        @Nullable
                @Schema(
                        description = "Date the book was returned, null if not yet returned",
                        example = "2026-05-18",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        nullable = true)
                LocalDate returnedDate,
        @Schema(
                        description = "ID of the book",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long bookId,
        @Nonnull
                @Schema(
                        description = "Title of the book",
                        example = "1984",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String bookTitle) {

    /**
     * Converts a {@link Loan Loan} to a DTO.
     *
     * @param loan the loan to convert
     * @return the converted DTO
     */
    public static LoanResponseV1 fromLoan(Loan loan) {
        return new LoanResponseV1(
                loan.getId(),
                loan.getPersonName(),
                loan.getLoanDate(),
                loan.getReturnedDate().orElse(null),
                loan.getBook().getId(),
                loan.getBook().getTitle());
    }
}
