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
                        example = "9780618056824",
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
                long authorId) {

    /**
     * Converts a {@link Book Book} to a DTO.
     *
     * @param book the book to convert
     * @return the converted DTO
     */
    public static BookResponseV1 fromBook(Book book) {
        return new BookResponseV1(
                book.getId(),
                book.getTitle(),
                book.getIsbn().orElse(null),
                book.getAuthor().getName(),
                book.getAuthor().getId());
    }
}
