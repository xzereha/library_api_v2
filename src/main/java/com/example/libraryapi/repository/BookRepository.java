package com.example.libraryapi.repository;

import com.example.libraryapi.model.Book;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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

    /**
     * Search books by optional ISBN, title (case-insensitive partial), and author name
     * (case-insensitive partial). All parameters are optional — null means "no filter".
     *
     * @param isbn exact ISBN match, or null
     * @param title case-insensitive partial title match, or null
     * @param authorName case-insensitive partial author name match, or null
     * @param pageable pagination information
     * @return a filtered paginated list of books
     */
    @Query(
            """
            SELECT b FROM Book b
            WHERE (:isbn IS NULL OR b.isbn = :isbn)
            AND (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
            AND (:authorName IS NULL OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :authorName, '%')))
            """)
    Page<Book> searchBooks(
            @Param("isbn") String isbn,
            @Param("title") String title,
            @Param("authorName") String authorName,
            Pageable pageable);

    /**
     * Find a book by ID with a pessimistic write lock.
     *
     * <p>Used to serialize concurrent loan creation for the same book and prevent race conditions
     * where two transactions both see no active loan and proceed to insert.
     *
     * @param id the ID of the book
     * @return an {@link Optional} containing the book or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithLock(@Param("id") Long id);
}
