package com.example.libraryapi.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

/** Request DTO for creating a {@link com.example.libraryapi.model.Book Book}. */
@Schema(description = "Request payload for creating a book.")
@Validated
public record BookRequestV1(
        @NotBlank(message = "Title is required.")
                @Schema(
                        description = "Title of the book",
                        example = "The Lord of the Rings",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String title,
        @Nullable
                @Schema(
                        description = "ISBN-13 of the book",
                        example = "978-0-618-05682-4",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        nullable = true)
                String isbn,
        @Min(value = 1, message = "Author ID must be a positive number.")
                @Schema(
                        description = "ID of the author",
                        example = "1",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long authorId) {}
