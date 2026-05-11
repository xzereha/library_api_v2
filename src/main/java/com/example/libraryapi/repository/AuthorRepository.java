package com.example.libraryapi.repository;

import com.example.libraryapi.model.Author;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Database repository for {@link com.example.libraryapi.model.Author Author}. */
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * Find an author by its ISNI.
     *
     * @param isni the ISNI of the author to find
     * @return an {@link Optional} containing the found author or empty if not found
     */
    Optional<Author> findByIsni(String isni);
}
