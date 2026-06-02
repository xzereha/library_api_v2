package com.example.libraryapi.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts and locks accounts after exceeding the maximum number of failed
 * attempts. Locked accounts remain locked for a configurable duration before being automatically
 * unlocked.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /**
     * Record a successful login for the given username. Clears any previous failed attempt records.
     *
     * @param username the username that successfully logged in
     */
    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    /**
     * Record a failed login attempt for the given username. If the number of failed attempts
     * reaches the maximum, the account is locked for the configured duration.
     *
     * @param username the username that failed to log in
     */
    public void loginFailed(String username) {
        Attempt attempt = attempts.computeIfAbsent(username, k -> new Attempt());
        attempt.count++;
        attempt.lastFailed = Instant.now();
        if (attempt.count >= MAX_ATTEMPTS) {
            attempt.lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    /**
     * Check if an account is currently locked due to too many failed login attempts. If the lock
     * duration has expired, the account is automatically unlocked.
     *
     * @param username the username to check
     * @return true if the account is locked, false otherwise
     */
    public boolean isLocked(String username) {
        Attempt attempt = attempts.get(username);
        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.lockedUntil)) {
            attempts.remove(username);
            return false;
        }
        return true;
    }

    private static class Attempt {
        int count;
        Instant lastFailed;
        Instant lockedUntil;
    }
}
