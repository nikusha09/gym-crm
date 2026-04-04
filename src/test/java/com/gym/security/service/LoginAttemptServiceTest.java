package com.gym.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("isBlocked: new user is not blocked")
    void isBlocked_newUser_returnsFalse() {
        assertFalse(loginAttemptService.isBlocked("John.Smith"));
    }

    @Test
    @DisplayName("loginFailed: user not blocked after 1 failure")
    void loginFailed_oneFailure_notBlocked() {
        loginAttemptService.loginFailed("John.Smith");
        assertFalse(loginAttemptService.isBlocked("John.Smith"));
    }

    @Test
    @DisplayName("loginFailed: user not blocked after 2 failures")
    void loginFailed_twoFailures_notBlocked() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        assertFalse(loginAttemptService.isBlocked("John.Smith"));
    }

    @Test
    @DisplayName("loginFailed: user blocked after 3 failures")
    void loginFailed_threeFailures_blocked() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        assertTrue(loginAttemptService.isBlocked("John.Smith"));
    }

    @Test
    @DisplayName("loginSucceeded: resets attempts, user no longer blocked")
    void loginSucceeded_afterBlock_resetsBlock() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        assertTrue(loginAttemptService.isBlocked("John.Smith"));

        loginAttemptService.loginSucceeded("John.Smith");
        assertFalse(loginAttemptService.isBlocked("John.Smith"));
    }

    @Test
    @DisplayName("loginFailed: different users are tracked independently")
    void loginFailed_differentUsers_trackedIndependently() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");

        assertTrue(loginAttemptService.isBlocked("John.Smith"));
        assertFalse(loginAttemptService.isBlocked("Jane.Doe"));
    }

    @Test
    @DisplayName("loginSucceeded: resets attempts so user can fail again")
    void loginSucceeded_resetsAttempts_canFailAgain() {
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginSucceeded("John.Smith");

        loginAttemptService.loginFailed("John.Smith");
        loginAttemptService.loginFailed("John.Smith");
        assertFalse(loginAttemptService.isBlocked("John.Smith"));
    }
}
