package com.gym.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    @DisplayName("isBlacklisted: non-blacklisted token returns false")
    void isBlacklisted_nonBlacklistedToken_returnsFalse() {
        assertFalse(tokenBlacklistService.isBlacklisted("some.jwt.token"));
    }

    @Test
    @DisplayName("blacklist: blacklisted token returns true")
    void blacklist_token_isBlacklisted() {
        tokenBlacklistService.blacklist("some.jwt.token");
        assertTrue(tokenBlacklistService.isBlacklisted("some.jwt.token"));
    }

    @Test
    @DisplayName("blacklist: different token is not blacklisted")
    void blacklist_differentToken_notBlacklisted() {
        tokenBlacklistService.blacklist("token.one");
        assertFalse(tokenBlacklistService.isBlacklisted("token.two"));
    }

    @Test
    @DisplayName("blacklist: multiple tokens can be blacklisted")
    void blacklist_multipleTokens_allBlacklisted() {
        tokenBlacklistService.blacklist("token.one");
        tokenBlacklistService.blacklist("token.two");

        assertTrue(tokenBlacklistService.isBlacklisted("token.one"));
        assertTrue(tokenBlacklistService.isBlacklisted("token.two"));
    }
}
