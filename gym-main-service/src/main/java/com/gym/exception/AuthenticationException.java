package com.gym.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String username) {
        super("Authentication failed for username: " + username);
    }
}
