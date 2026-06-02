package com.example.libraryapi.config;

import com.example.libraryapi.service.LoginAttemptService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

/**
 * Filter that checks whether the authenticated user's account is locked before allowing the request
 * to proceed. Runs before {@code UsernamePasswordAuthenticationFilter}.
 */
public class LoginAttemptFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";
    private static final String JSON_423 =
            "{\"status\":423,\"error\":\"Locked\","
                    + "\"message\":\"Account locked due to too many failed attempts."
                    + " Try again in 15 minutes.\"}";

    private final LoginAttemptService loginAttemptService;

    /**
     * Constructor.
     *
     * @param loginAttemptService the service to check whether accounts are locked
     */
    public LoginAttemptFilter(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BASIC_PREFIX)) {
            try {
                String base64 = authHeader.substring(BASIC_PREFIX.length()).trim();
                String decoded = new String(Base64.getDecoder().decode(base64));
                String username = decoded.split(":")[0];
                if (loginAttemptService.isLocked(username)) {
                    response.setStatus(423);
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write(JSON_423);
                    return;
                }
            } catch (IllegalArgumentException e) {
                // Invalid Base64 — let authentication fail naturally
            }
        }

        chain.doFilter(request, response);
    }
}
