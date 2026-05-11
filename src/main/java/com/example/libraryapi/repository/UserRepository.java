package com.example.libraryapi.repository;

import com.example.libraryapi.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Database repository for {@link User}. */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the found user or empty
     */
    Optional<User> findByUsername(String username);
}
