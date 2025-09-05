package com.gcc.app.security;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BruteForceProtectionService {
    private final int maxAttempts;
    private final Cache<String, Integer> loginAttemptsCache;

    public BruteForceProtectionService(@Value("${bruteforce.max-attempts}") int maxAttempts,
                                       Cache<String, Integer> loginAttemptsCache) {
        this.maxAttempts = maxAttempts;
        this.loginAttemptsCache = loginAttemptsCache;
    }

    public boolean isBlocked(String username) {
        Integer attempts = loginAttemptsCache.getIfPresent(username);

        return attempts != null && attempts >= maxAttempts;
    }

    public void recordFailedAttempt(String username) {
        Integer attempts = loginAttemptsCache.getIfPresent(username);

        if (attempts == null) {
            attempts = 0;
        }

        attempts++;
        loginAttemptsCache.put(username, attempts);
        log.info("Failed login attempt {} for {}", attempts, username);
    }

    public void resetAttempts(String username) {
        loginAttemptsCache.invalidate(username);
        log.info("Reset login attempts for {}", username);
    }
}