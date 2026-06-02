package com.example.libraryapi.config;

import com.example.libraryapi.service.LoginAttemptService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Security configuration for the Library API. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String[] SWAGGER_PATHS = {"/swagger-ui/**", "/api-docs/**"};
    private static final String[] H2_PATHS = {"/h2-console/**"};

    private final LoginAttemptService loginAttemptService;

    /**
     * Constructor.
     *
     * @param loginAttemptService the service to check for locked accounts
     */
    public SecurityConfig(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Provides a BCrypt-based password encoder for hashing and verifying passwords.
     *
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain with rate limiting, authorization rules, CORS, and
     * security headers.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(
                        new LoginAttemptFilter(loginAttemptService),
                        UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_PATHS).hasRole(ADMIN)
                        .requestMatchers(H2_PATHS).hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/authors").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/books").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole(ADMIN)
                        .requestMatchers("/api/v1/loans/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny()));
        return http.build();
    }
}
