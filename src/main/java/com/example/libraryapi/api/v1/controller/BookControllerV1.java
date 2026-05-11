package com.example.libraryapi.api.v1.controller;

import com.example.libraryapi.api.ErrorResponse;
import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.Response;
import com.example.libraryapi.api.v1.dto.BookRequestV1;
import com.example.libraryapi.api.v1.dto.BookResponseV1;
import com.example.libraryapi.api.v1.facade.BookFacadeV1;

import io.swagger.v3.oas.annotations.Operation;
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
     * Endpoint for retrieving all books with pagination support.
     *
     * @param pageable pagination information
     * @return A paginated response of books
     */
    @Operation(summary = "Get all books with pagination")
    @GetMapping("/books")
    public ResponseEntity<PagedResponse<BookResponseV1>> getAll(Pageable pageable) {
        return ResponseEntity.ok(facade.getBooks(pageable));
    }

    /**
     * Endpoint for retrieving a single book by ID.
     *
     * @param id The ID of the book to retrieve.
     * @return A ResponseEntity containing a Response object with the retrieved book.
     */
    @Operation(summary = "Get a book by ID")
    @ApiResponse(
            responseCode = "200",
            description = "The retrieved book",
            content = @Content(schema = @Schema(implementation = BookResponseV1.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/books/{id}")
    public ResponseEntity<Response<BookResponseV1>> getBookById(
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
            summary = "Create a book.",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "The created book",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BookResponseV1.class))
                        }),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid request body",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "Author not found",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProblemDetail.class))
                        })
            })
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
    @Operation(summary = "Get books by author ID")
    @ApiResponse(
            responseCode = "200",
            description = "Paginated list of books by the author",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @GetMapping("/authors/{authorId}/books")
    public ResponseEntity<PagedResponse<BookResponseV1>> getBooksByAuthor(
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
    @Operation(summary = "Delete a book by ID")
    @ApiResponse(
            responseCode = "204",
            description = "Book deleted successfully")
    @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable(name = "id", required = true) final long id) {
        facade.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
