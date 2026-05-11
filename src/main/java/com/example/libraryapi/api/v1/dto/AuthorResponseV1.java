package com.example.libraryapi.api.v1.dto;

import com.example.libraryapi.model.Author;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.springframework.validation.annotation.Validated;

/** Response DTO for {@link com.example.libraryapi.model.Author Author}. */
@Schema(description = "Response payload for an author.")
@Validated
public record AuthorResponseV1(
        @Nonnull
                @Schema(
                        description = "Unique identifier of the author",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                Long id,
        @Nonnull
                @Schema(
                        description = "Name of the author",
                        example = "J.R.R. Tolkien",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String name,
        @Schema(
                        description = "ISNI of the author",
                        example = "0000-0001-2345-6789",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        nullable = true)
                @Nullable
                String isni) {

    /**
     * Converts an {@link com.example.libraryapi.model.Author Author} to a DTO.
     *
     * @param author the author to convert
     * @return the converted DTO
     */
    public static AuthorResponseV1 fromAuthor(Author author) {
        return new AuthorResponseV1(
                author.getId(), author.getName(), author.getIsni().orElse(null));
    }
}
