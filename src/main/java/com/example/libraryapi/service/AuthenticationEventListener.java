package com.example.libraryapi.service;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Listens for Spring Security authentication events and delegates to {@link LoginAttemptService}
 * to track successful and failed login attempts.
 */
@Component
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    /**
     * Constructor.
     *
     * @param loginAttemptService the service to track login attempts
     */
    public AuthenticationEventListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Handles successful authentication events.
     *
     * @param event the authentication success event
     */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        loginAttemptService.loginSucceeded(event.getAuthentication().getName());
    }

    /**
     * Handles authentication failure events caused by bad credentials.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        loginAttemptService.loginFailed(event.getAuthentication().getName());
    }
}
