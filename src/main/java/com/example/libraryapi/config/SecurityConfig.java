package com.example.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Security configuration for the Library API. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";

    /**
     * Configure the security filter chain.
     *
     * <p>POST/DELETE /api/v1/authors and POST/DELETE /api/v1/books require ADMIN role.
     * All /api/v1/loans/** endpoints require authentication (ownership is verified in the
     * facade). All other endpoints are accessible without authentication.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/authors").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/books").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole(ADMIN)
                        .requestMatchers("/api/v1/loans/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
