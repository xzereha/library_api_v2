package com.example.libraryapi.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

/** Author Entity. */
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Size(min = 16, max = 16, message = "ISNI number must be 16 characters")
    @Nullable
    @Column(nullable = true)
    private String isni;

    /** Default constructor for JPA. Should not be used directly */
    public Author() {
        // Empty constructor for JPA
    }

    /**
     * Constructor for a new Author.
     *
     * @param name Name of the Author
     * @param isni Valid ISNI of the Author or null
     */
    public Author(@NotBlank String name, @Nullable String isni) {
        setName(name);
        setIsni(isni);
    }

    public String getName() {
        return name;
    }

    /**
     * Update the name of the Author.
     *
     * @param name New name of the Author, must not be blank.
     */
    public void setName(@NotBlank String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
    }

    public Optional<String> getIsni() {
        return Optional.ofNullable(isni);
    }

    /**
     * Sets the ISNI number of the Author.
     *
     * <p>The format must match valid ISNI with 16 digits, spaces between them is accepted.
     *
     * <p>Example: <i>0000 0000 0000 0000</i>
     *
     * <p>If null is passed in then the ISNI is removed.
     *
     * @param isni ISNI for the Author, or null to clear.
     */
    public void setIsni(@Nullable String isni) {
        if (isni == null) {
            this.isni = isni;
            return;
        }
        String compactedIsni = isni.replaceAll("\\s", "");
        if (compactedIsni.length() != 16) {
            throw new IllegalArgumentException("ISNI must be exactly 16 digits long.");
        }
        this.isni = compactedIsni;
    }
}
