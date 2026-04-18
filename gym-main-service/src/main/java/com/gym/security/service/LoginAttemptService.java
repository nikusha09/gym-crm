package com.gym.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_DURATION_MINUTES = 5;

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> blockedUntil = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        if (!blockedUntil.containsKey(username)) {
            return false;
        }

        if (LocalDateTime.now().isAfter(blockedUntil.get(username))) {
            log.debug("Block expired for username: {}", username);
            attempts.remove(username);
            blockedUntil.remove(username);
            return false;
        }

        log.warn("User is blocked | username={} | blockedUntil={}",
                username, blockedUntil.get(username));
        return true;
    }

    public void loginFailed(String username) {
        int currentAttempts = attempts.merge(username, 1, Integer::sum);
        log.warn("Failed login attempt | username={} | attempts={}", username, currentAttempts);

        if (currentAttempts >= MAX_ATTEMPTS) {
            LocalDateTime blockTime = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES);
            blockedUntil.put(username, blockTime);
            log.warn("User blocked | username={} | until={}", username, blockTime);
        }
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
        blockedUntil.remove(username);
        log.debug("Login succeeded, attempts reset | username={}", username);
    }
}
