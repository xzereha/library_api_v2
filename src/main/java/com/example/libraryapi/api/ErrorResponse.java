package com.example.libraryapi.api;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;

/** Response for an error. */
@Schema(description = "Response for an error")
public record ErrorResponse(
        @Nonnull
                @Schema(
                        description = "Status of the response",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "400",
                        nullable = false)
                int status,
        @Nonnull
                @Schema(
                        description = "Data of the response",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "An error occurred.",
                        nullable = false)
                String message,
        @Min(1)
                @Schema(
                        description = "API version",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int version) {}
