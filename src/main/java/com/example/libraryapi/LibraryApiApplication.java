package com.example.libraryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entrypoint.
 */
@SpringBootApplication
@EnableCaching
public class LibraryApiApplication {

    /**
     * Entrypoint.
     * This is just used to launch Spring.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(LibraryApiApplication.class, args);
    }

}
