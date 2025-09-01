package com.gcs.app.security;

import com.gcs.app.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupTaskTest {

    @Mock
    private RefreshTokenRepository repository;

    @InjectMocks
    private RefreshTokenCleanupTask cleanupTask;

    @Test
    void deleteExpiredTokens_shouldCallRepositoryWithCurrentInstant() {
        when(repository.deleteAllByExpiryDateBefore(any(Instant.class))).thenReturn(3);

        cleanupTask.deleteExpiredTokens();

        verify(repository).deleteAllByExpiryDateBefore(any(Instant.class));
    }
}