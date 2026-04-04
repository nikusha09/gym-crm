package com.gym.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", EXPIRATION);
    }

    @Test
    @DisplayName("generateToken: returns non-null non-blank token")
    void generateToken_returnsValidToken() {
        String token = jwtUtil.generateToken("John.Smith");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("extractUsername: returns correct username from token")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("John.Smith");

        assertEquals("John.Smith", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    @DisplayName("isTokenValid: valid token and matching user returns true")
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtUtil.generateToken("John.Smith");
        UserDetails userDetails = User.builder()
                .username("John.Smith")
                .password("password")
                .authorities(List.of())
                .build();

        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("isTokenValid: token with wrong username returns false")
    void isTokenValid_wrongUsername_returnsFalse() {
        String token = jwtUtil.generateToken("John.Smith");
        UserDetails userDetails = User.builder()
                .username("Jane.Doe")
                .password("password")
                .authorities(List.of())
                .build();

        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("isTokenValid: expired token returns false")
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000L);
        String token = jwtUtil.generateToken("John.Smith");

        UserDetails userDetails = User.builder()
                .username("John.Smith")
                .password("password")
                .authorities(List.of())
                .build();

        assertFalse(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("extractUsername: different users produce different tokens")
    void generateToken_differentUsers_producesDifferentTokens() {
        String token1 = jwtUtil.generateToken("John.Smith");
        String token2 = jwtUtil.generateToken("Jane.Doe");

        assertNotEquals(token1, token2);
        assertEquals("John.Smith", jwtUtil.getUsernameFromToken(token1));
        assertEquals("Jane.Doe", jwtUtil.getUsernameFromToken(token2));
    }
}
