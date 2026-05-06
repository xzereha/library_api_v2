package com.example.libraryapi.repository;

import com.example.libraryapi.model.Author;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database repository for {@link com.example.libraryapi.model.Author Author}. */
public interface AuthorRepository extends JpaRepository<Author, Long> {}
