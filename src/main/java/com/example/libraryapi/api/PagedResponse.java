package com.example.libraryapi.api;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Generic wrapper for paginated API responses, containing a list of data items and pagination
 * metadata.
 */
@Schema(
        description =
                "Wrapper for paginated API responses, including data, pagination metadata, and"
                        + " version information")
public record PagedResponse<T>(
        @Nonnull
                @Schema(
                        description = "Data of the response",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                List<T> data,
        @Schema(
                        description = "Current page number (0-indexed)",
                        example = "0",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int page,
        @Schema(
                        description = "Number of elements per page",
                        example = "20",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int size,
        @Schema(
                        description = "Total number of elements across all pages",
                        example = "100",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                long totalElements,
        @Schema(
                        description = "Total number of pages",
                        example = "5",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int totalPages,
        @Min(1)
                @Schema(
                        description = "API version",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        nullable = false)
                int version) {}
