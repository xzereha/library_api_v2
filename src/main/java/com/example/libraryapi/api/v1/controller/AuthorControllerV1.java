package com.example.libraryapi.api.v1.controller;

import com.example.libraryapi.api.ErrorResponse;
import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.Response;
import com.example.libraryapi.api.v1.dto.AuthorRequestV1;
import com.example.libraryapi.api.v1.dto.AuthorResponseV1;
import com.example.libraryapi.api.v1.facade.AuthorFacadeV1;

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

/** API controller for {@link com.example.libraryapi.model.Author Author}. */
@RestController
@RequestMapping("/api/v" + AuthorControllerV1.VERSION + "/authors")
public class AuthorControllerV1 {
    static final int VERSION = 1;
    private final AuthorFacadeV1 facade;

    /**
     * Constructor for the controller.
     *
     * @param facadeV1 The facade to use for author operations
     */
    public AuthorControllerV1(AuthorFacadeV1 facadeV1) {
        this.facade = facadeV1;
    }

    /**
     * Endpoint for retrieving all authors with pagination support.
     *
     * @param pageable pagination information
     * @return A paginated response of authors
     */
    @Operation(summary = "Get all authors with pagination")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedResponse<AuthorResponseV1>> getAll(Pageable pageable) {
        return ResponseEntity.ok(facade.getAuthors(pageable));
    }

    /**
     * Endpoint for retrieving a single author by ID.
     *
     * @param id The ID of the author to retrieve.
     * @return A ResponseEntity containing a Response object with the retrieved author.
     */
    @Operation(summary = "Get an author by ID")
    @ApiResponse(
            responseCode = "200",
            description = "The retrieved author",
            content = @Content(schema = @Schema(implementation = AuthorResponseV1.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Author not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<AuthorResponseV1>> getAuthorById(
            @PathVariable(name = "id", required = true) final long id) {
        return ResponseEntity.ok(new Response<>(facade.getAuthor(id), VERSION));
    }

    /**
     * Endpoint for creating new authors.
     *
     * @param authorRequest The request payload for the new author.
     * @return The response payload for the new author.
     */
    @Operation(
            summary = "Create an author.",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "The created author",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthorResponseV1.class))
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
                        responseCode = "409",
                        description = "Author already exists",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                        })
            })
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response<AuthorResponseV1>> createAuthor(
            @Valid @RequestBody final AuthorRequestV1 authorRequest) {
        var response = facade.createAuthor(authorRequest);
        var location = URI.create("/api/v" + VERSION + "/authors/" + response.id());
        return ResponseEntity.created(location).body(new Response<>(response, VERSION));
    }

    /**
     * Endpoint for deleting an author by ID.
     *
     * @param id The ID of the author to delete.
     * @return A 204 No Content response if the author was deleted.
     */
    @Operation(summary = "Delete an author by ID")
    @ApiResponse(
            responseCode = "204",
            description = "Author deleted successfully")
    @ApiResponse(
            responseCode = "404",
            description = "Author not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable(name = "id", required = true) final long id) {
        facade.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}
