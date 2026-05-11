package com.example.libraryapi.api.v1.facade;

import com.example.libraryapi.api.v1.dto.AuthorRequestV1;
import com.example.libraryapi.api.v1.dto.AuthorResponseV1;
import com.example.libraryapi.exception.AuthorNotFoundException;
import com.example.libraryapi.service.AuthorService;

import lombok.NonNull;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Facade for {@link com.example.libraryapi.service.AuthorService AuthorService}.
 *
 * <p>This facade is used for mapping between the versioned DTOs and the service layer.
 */
@Service
public class AuthorFacadeV1 {
    private final AuthorService authorService;

    /**
     * Constructor.
     *
     * @param authorService the service to use for managing authors
     */
    public AuthorFacadeV1(AuthorService authorService) {
        this.authorService = authorService;
    }

    public List<AuthorResponseV1> getAuthors() {
        return authorService.getAll().stream().map(AuthorResponseV1::fromAuthor).toList();
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
}
