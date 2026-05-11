package com.example.libraryapi.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import lombok.NonNull;

import org.springframework.validation.annotation.Validated;

/** Request DTO for creating an {@link com.example.libraryapi.model.Author Author}. */
@Schema(description = "Request payload for creating an author.")
@Validated
public record AuthorRequestV1(
        @NonNull
                @NotBlank(message = "Name is required.")
                @Schema(
                        description = "Name of the author",
                        example = "J.R.R. Tolkien",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                String name,
        @Nullable
                @Schema(
                        description = "ISNI of the author (16 digits, spaces allowed)",
                        example = "0000 0001 2135 1230",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        nullable = true)
                String isni) {}
