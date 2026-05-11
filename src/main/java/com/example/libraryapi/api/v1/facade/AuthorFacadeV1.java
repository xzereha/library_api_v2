package com.example.libraryapi.api.v1.facade;

import com.example.libraryapi.api.PagedResponse;
import com.example.libraryapi.api.v1.dto.AuthorRequestV1;
import com.example.libraryapi.api.v1.dto.AuthorResponseV1;
import com.example.libraryapi.exception.AuthorNotFoundException;
import com.example.libraryapi.service.AuthorService;

import lombok.NonNull;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade for {@link com.example.libraryapi.service.AuthorService AuthorService}.
 *
 * <p>This facade is used for mapping between the versioned DTOs and the service layer.
 */
@Service
public class AuthorFacadeV1 {
    static final int VERSION = 1;
    private final AuthorService authorService;

    /**
     * Constructor.
     *
     * @param authorService the service to use for managing authors
     */
    public AuthorFacadeV1(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Get all authors with pagination.
     *
     * @param pageable pagination information
     * @return a paginated response of authors
     */
    public PagedResponse<AuthorResponseV1> getAuthors(Pageable pageable) {
        var page = authorService.getAll(pageable);
        var authors = page.getContent().stream().map(AuthorResponseV1::fromAuthor).toList();
        return new PagedResponse<>(
                authors,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                VERSION);
    }

    /**
     * Get an author by ID.
     *
     * @param id Id to retrieve
     * @return The found author.
     * @throws AuthorNotFoundException if no author with the given ID exists
     */
    public AuthorResponseV1 getAuthor(Long id) {
        return authorService
                .getAuthorById(id)
                .map(AuthorResponseV1::fromAuthor)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    /**
     * Create an author.
     *
     * @param request the request DTO containing the data for creating the author
     * @return the response DTO containing the created author
     */
    public AuthorResponseV1 createAuthor(@NonNull AuthorRequestV1 request) {
        var author =
                authorService.createAuthor(request.name(), Optional.ofNullable(request.isni()));
        return new AuthorResponseV1(
                author.getId(), author.getName(), author.getIsni().orElse(null));
    }

    /**
     * Delete an author by ID.
     *
     * @param id the ID of the author to delete
     * @throws AuthorNotFoundException if no author with the given ID exists
     */
    public void deleteAuthor(long id) {
        authorService
                .getAuthorById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        authorService.deleteAuthor(id);
    }
}
