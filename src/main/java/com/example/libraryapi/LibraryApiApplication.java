package com.example.libraryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entrypoint.
 */
@SpringBootApplication
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
