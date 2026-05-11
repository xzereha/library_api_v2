package com.example.libraryapi.api;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;

/**
 * Generic wrapper for API responses, containing the actual data and metadata such as API version.
 */
@Schema(description = "Wrapper for API responses, including data and version information")
public record Response<T>(
        @Nonnull
                @Schema(
                        description = "Data of the response",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                T data,
        @Min(1)
                @Schema(
                        description = "API version",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int version) {}
