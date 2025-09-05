package com.gcc.app.security;

import com.gcc.app.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private final RefreshTokenRepository repository;

    @Scheduled(fixedRateString = "${refresh-token.cleanup.every-milliseconds:3600000}")
    @Transactional
    public void deleteExpiredTokens() {
        int deletedCount = repository.deleteAllByExpiryDateBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }
}