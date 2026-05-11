package com.example.libraryapi.repository;

import com.example.libraryapi.model.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database repository for {@link Book Book}. */
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find all books by a given author ID, with pagination support.
     *
     * @param authorId the ID of the author
     * @param pageable pagination information
     * @return a paginated list of books by the author
     */
    Page<Book> findByAuthorId(long authorId, Pageable pageable);
}
