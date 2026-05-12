package com.example.libraryapi.api.v1.dto;

import com.example.libraryapi.model.Book;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.springframework.validation.annotation.Validated;

/** Response DTO for {@link com.example.libraryapi.model.Book Book}. */
@Schema(description = "Response payload for a book.")
@Validated
public record BookResponseV1(
        @Nonnull
                @Schema(
                        description = "Unique identifier of the book",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                Long id,
        @Nonnull
                @Schema(
                        description = "Title of the book",
                        example = "The Lord of the Rings",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String title,
        @Schema(
                        description = "ISBN-13 of the book",
                        example = "978-0008471286",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        nullable = true)
                @Nullable
                String isbn,
        @Nonnull
                @Schema(
                        description = "Name of the author",
                        example = "J.R.R. Tolkien",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String authorName,
        @Schema(
                        description = "ID of the author",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long authorId,
        @Schema(
                        description = "Whether the book is available for borrowing",
                        example = "true",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                boolean isAvailable) {

    /**
     * Converts a {@link Book Book} to a DTO with availability information.
     *
     * @param book the book to convert
     * @param isAvailable whether the book is available for borrowing
     * @return the converted DTO
     */
    public static BookResponseV1 fromBook(Book book, boolean isAvailable) {
        return new BookResponseV1(
                book.getId(),
                book.getTitle(),
                book.getIsbn().orElse(null),
                book.getAuthor().getName(),
                book.getAuthor().getId(),
                isAvailable);
    }
}
