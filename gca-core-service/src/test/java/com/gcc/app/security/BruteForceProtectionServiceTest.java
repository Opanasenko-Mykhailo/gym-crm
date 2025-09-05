package com.gcc.app.security;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionServiceTest {

    private static final String USERNAME = "test.user";
    private static final int MAX_ATTEMPTS = 3;

    private BruteForceProtectionService service;

    @Mock
    private Cache<String, Integer> loginAttemptsCache;

    @Captor
    private ArgumentCaptor<Integer> attemptsCaptor;

    @BeforeEach
    void setUp() {
        service = new BruteForceProtectionService(MAX_ATTEMPTS, loginAttemptsCache);
    }

    @Test
    void isBlocked_whenAttemptsAreNull_returnsFalse() {
        when(loginAttemptsCache.getIfPresent(USERNAME)).thenReturn(null);

        boolean actual = service.isBlocked(USERNAME);

        assertFalse(actual);
        verify(loginAttemptsCache).getIfPresent(USERNAME);
    }

    @Test
    void isBlocked_whenAttemptsLessThanMax_returnsFalse() {
        when(loginAttemptsCache.getIfPresent(USERNAME)).thenReturn(MAX_ATTEMPTS - 1);

        boolean actual = service.isBlocked(USERNAME);

        assertFalse(actual);
        verify(loginAttemptsCache).getIfPresent(USERNAME);
    }

    @Test
    void isBlocked_whenAttemptsEqualToMax_returnsTrue() {
        when(loginAttemptsCache.getIfPresent(USERNAME)).thenReturn(MAX_ATTEMPTS);

        boolean actual = service.isBlocked(USERNAME);

        assertTrue(actual);
        verify(loginAttemptsCache).getIfPresent(USERNAME);
    }

    @Test
    void recordFailedAttempt_whenNoPreviousAttempts_addsOne() {
        when(loginAttemptsCache.getIfPresent(USERNAME)).thenReturn(null);

        service.recordFailedAttempt(USERNAME);

        verify(loginAttemptsCache).put(eq(USERNAME), attemptsCaptor.capture());
        assertEquals(1, attemptsCaptor.getValue().intValue());
    }

    @Test
    void recordFailedAttempt_whenPreviousAttempts_exist_increments() {
        when(loginAttemptsCache.getIfPresent(USERNAME)).thenReturn(2);

        service.recordFailedAttempt(USERNAME);

        verify(loginAttemptsCache).put(eq(USERNAME), attemptsCaptor.capture());
        assertEquals(3, attemptsCaptor.getValue().intValue());
    }

    @Test
    void resetAttempts_invokesInvalidate() {
        doNothing().when(loginAttemptsCache).invalidate(USERNAME);

        service.resetAttempts(USERNAME);

        verify(loginAttemptsCache).invalidate(USERNAME);
    }
}