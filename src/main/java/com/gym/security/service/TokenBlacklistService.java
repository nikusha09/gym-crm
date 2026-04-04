package com.gym.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Boolean> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token) {
        blacklist.put(token, true);
        log.debug("Token blacklisted");
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }
}
