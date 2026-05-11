package com.example.libraryapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/** Application user entity backed by the database. */
@Entity
@Table(name = "library_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Column(nullable = false)
    private String role;

    /** Default constructor for JPA. Should not be used directly. */
    protected User() {}

    /**
     * Constructor for a new User.
     *
     * @param username the unique username
     * @param password the encoded password
     * @param role     the role (e.g. "ADMIN" or "USER")
     */
    public User(@NotBlank String username, @NotBlank String password, @NotBlank String role) {
        setUsername(username);
        setPassword(password);
        setRole(role);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Update the username.
     *
     * @param username new username, must not be blank
     */
    public void setUsername(@NotBlank String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Update the password.
     *
     * @param password new password, must not be blank
     */
    public void setPassword(@NotBlank String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    /**
     * Update the role.
     *
     * @param role new role, must not be blank
     */
    public void setRole(@NotBlank String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role must not be empty");
        }
        this.role = role;
    }
}
