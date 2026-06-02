package com.example.libraryapi.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    @DisplayName("should not lock on first failed attempt")
    void shouldNotLockOnFirstFailure() {
        service.loginFailed("user");
        assertFalse(service.isLocked("user"));
    }

    @Test
    @DisplayName("should lock after 5 failed attempts")
    void shouldLockAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("user");
        }
        assertTrue(service.isLocked("user"));
    }

    @Test
    @DisplayName("should unlock after successful login")
    void shouldUnlockAfterSuccess() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("user");
        }
        assertTrue(service.isLocked("user"));
        service.loginSucceeded("user");
        assertFalse(service.isLocked("user"));
    }

    @Test
    @DisplayName("should not lock unknown user")
    void shouldNotLockUnknown() {
        assertFalse(service.isLocked("unknown"));
    }

    @Test
    @DisplayName("should handle multiple users independently")
    void shouldHandleMultipleUsersIndependently() {
        for (int i = 0; i < 5; i++) {
            service.loginFailed("attacker");
        }
        assertTrue(service.isLocked("attacker"));
        assertFalse(service.isLocked("legit"));
    }
}
