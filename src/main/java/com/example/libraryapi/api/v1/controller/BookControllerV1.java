package com.example.libraryapi.api.v1.controller;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.Response;
import com.example.libraryapi.api.v1.dto.BookRequestV1;
import com.example.libraryapi.api.v1.dto.BookResponseV1;
import com.example.libraryapi.api.v1.facade.BookFacadeV1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** API controller for {@link com.example.libraryapi.model.Book Book}. */
@RestController
@RequestMapping("/api/v" + BookControllerV1.VERSION)
public class BookControllerV1 {
    static final int VERSION = 1;
    private final BookFacadeV1 facade;

    /**
     * Constructor for the controller.
     *
     * @param facade The facade to use for book operations
     */
    public BookControllerV1(BookFacadeV1 facade) {
        this.facade = facade;
    }

    /**
     * Endpoint for retrieving all books with pagination support and optional filters.
     *
     * @param isbn optional exact ISBN match
     * @param title optional case-insensitive partial title match
     * @param authorName optional case-insensitive partial author name match
     * @param pageable pagination information
     * @return A paginated response of books
     */
    @Operation(
            summary = "Get all books",
            description = "Returns a paginated list of books, optionally filtered by ISBN, title"
                    + " (case-insensitive partial), or author name (case-insensitive partial).")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of books retrieved successfully")
    @GetMapping("/books")
    public ResponseEntity<PagedResponse<BookResponseV1>> getAll(
            @RequestParam(required = false)
            @Parameter(description = "Exact ISBN match", example = "9780141037145")
            String isbn,
            @RequestParam(required = false)
            @Parameter(description = "Case-insensitive partial title match", example = "lord")
            String title,
            @RequestParam(required = false)
            @Parameter(
                    description = "Case-insensitive partial author name match",
                    example = "orwell")
            String authorName,
            Pageable pageable) {
        return ResponseEntity.ok(facade.getBooks(isbn, title, authorName, pageable));
    }

    /**
     * Endpoint for retrieving a single book by ID.
     *
     * @param id The ID of the book to retrieve.
     * @return A ResponseEntity containing a Response object with the retrieved book.
     */
    @Operation(summary = "Get a book by ID", description = "Returns a single book by its ID.")
    @ApiResponse(
            responseCode = "200",
            description = "The book was found and returned successfully")
    @ApiResponse(
            responseCode = "404",
            description = "No book with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/books/{id}")
    public ResponseEntity<Response<BookResponseV1>> getBookById(
            @Parameter(description = "ID of the book to retrieve", example = "1", required = true)
            @PathVariable(name = "id", required = true) final long id) {
        return ResponseEntity.ok(new Response<>(facade.getBook(id), VERSION));
    }

    /**
     * Endpoint for creating new books.
     *
     * @param request The request payload for the new book.
     * @return The response payload for the new book.
     */
    @Operation(
            summary = "Create a book",
            description = "Creates a new book and returns the created resource. Requires ADMIN role.")
    @ApiResponse(
            responseCode = "201",
            description = "Book created successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request body – missing or malformed fields",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions – ADMIN role required",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "No author with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(
            value = "/books",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<BookResponseV1>> createBook(
            @Valid @RequestBody final BookRequestV1 request) {
        var response = facade.createBook(request);
        var location = URI.create("/api/v" + VERSION + "/books/" + response.id());
        return ResponseEntity.created(location).body(new Response<>(response, VERSION));
    }

    /**
     * Endpoint for retrieving all books by a specific author.
     *
     * @param authorId The ID of the author to retrieve books for.
     * @param pageable Pagination information.
     * @return A paginated response of books by the author.
     */
    @Operation(
            summary = "Get books by author ID",
            description = "Returns a paginated list of books written by a specific author.")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of books by the author retrieved successfully")
    @ApiResponse(
            responseCode = "404",
            description = "No author with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/authors/{authorId}/books")
    public ResponseEntity<PagedResponse<BookResponseV1>> getBooksByAuthor(
            @Parameter(description = "ID of the author", example = "1", required = true)
            @PathVariable(name = "authorId", required = true) final long authorId,
            Pageable pageable) {
        return ResponseEntity.ok(facade.getBooksByAuthorId(authorId, pageable));
    }

    /**
     * Endpoint for deleting a book by ID.
     *
     * @param id The ID of the book to delete.
     * @return A 204 No Content response if the book was deleted.
     */
    @Operation(
            summary = "Delete a book by ID",
            description = "Deletes a single book by its ID. Requires ADMIN role.")
    @ApiResponse(
            responseCode = "204",
            description = "Book deleted successfully")
    @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions – ADMIN role required",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "No book with the given ID exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", example = "1", required = true)
            @PathVariable(name = "id", required = true) final long id) {
        facade.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
