package com.gcc.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TokenHasherTest {

    private final TokenHasher tokenHasher = new TokenHasher();

    @Test
    @DisplayName("Given known token, when hashed, then returns expected hash")
    void givenKnownToken_whenHashed_thenReturnsExpectedHash() {
        String token = "my-secret-token";
        String expectedHash = "6lrdV0N8vyCvWQNNftF5aNzFZ2e0GWX8xbN21F24tKM=";

        String actualHash = tokenHasher.hash(token);

        assertEquals(expectedHash, actualHash);
    }

    @Test
    @DisplayName("Given same token, when hashed multiple times, then returns same hash")
    void givenSameToken_whenHashedMultipleTimes_thenReturnsSameHash() {
        String token = "repeated-token";

        String hash1 = tokenHasher.hash(token);
        String hash2 = tokenHasher.hash(token);

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Given different tokens, when hashed, then returns different hashes")
    void givenDifferentTokens_whenHashed_thenReturnsDifferentHashes() {
        String hash1 = tokenHasher.hash("token-1");
        String hash2 = tokenHasher.hash("token-2");

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Given any token, when hashed, then hash is not null")
    void givenAnyToken_whenHashed_thenHashIsNotNull() {
        String hash = tokenHasher.hash("any-token");

        assertNotNull(hash);
    }
}