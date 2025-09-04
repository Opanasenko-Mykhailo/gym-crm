package com.gcc.app.repository;

import com.gcc.app.model.RefreshTokenEntity;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private RefreshTokenRepository repository;

    @Test
    @DataSet(value = "dataset/refresh-token-data.xml", cleanBefore = true, cleanAfter = true)
    void findByToken_existingToken_returnsEntity() {
        String tokenHash = "tokenHash123";

        Optional<RefreshTokenEntity> actual = repository.findByToken(tokenHash);

        assertTrue(actual.isPresent());
        assertEquals(tokenHash, actual.get().getToken());
    }

    @Test
    @DataSet(value = "dataset/refresh-token-data.xml", cleanBefore = true, cleanAfter = true)
    void findByToken_absentToken_returnsEmptyOptional() {
        String tokenHash = "nonExistingToken";

        Optional<RefreshTokenEntity> actual = repository.findByToken(tokenHash);

        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/refresh-token-data.xml", cleanBefore = true, cleanAfter = true)
    void deleteByToken_existingToken_deletesEntity() {
        String tokenHash = "tokenHash123";

        repository.deleteByToken(tokenHash);

        Optional<RefreshTokenEntity> actual = repository.findByToken(tokenHash);
        assertFalse(actual.isPresent());
    }

    @Test
    @DataSet(value = "dataset/refresh-token-data.xml", cleanBefore = true, cleanAfter = true)
    void deleteAllByExpiryDateBefore_removesExpiredTokens() {
        Instant cutoff = Instant.parse("2023-01-01T00:00:00Z");

        int deletedCount = repository.deleteAllByExpiryDateBefore(cutoff);

        assertEquals(1, deletedCount);
    }
}