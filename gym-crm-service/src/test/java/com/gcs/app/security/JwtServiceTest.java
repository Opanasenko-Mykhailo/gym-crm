package com.gcs.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;
    private static final long REFRESH_EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationTime", EXPIRATION_TIME);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationTime", REFRESH_EXPIRATION_TIME);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String username = "test.user";
        Set<String> roles = Set.of("ROLE_USER");

        String token = jwtService.generateToken(username, roles);

        assertNotNull(token);
    }

    @Test
    void generateRefreshToken_shouldReturnNonNullToken() {
        String username = "test.user";

        String token = jwtService.generateRefreshToken(username);

        assertNotNull(token);
        assertEquals("refresh", jwtService.getTokenType(token));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String username = "test.user";
        Set<String> roles = Set.of("ROLE_USER");
        String token = jwtService.generateToken(username, roles);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        String username = "validUser";
        Set<String> roles = Set.of("ROLE_USER");
        String token = jwtService.generateToken(username, roles);

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        String username = "expiredUser";
        Set<String> roles = Set.of("ROLE_USER");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 0L);
        String token = jwtService.generateToken(username, roles);

        boolean isValid = jwtService.isTokenValid(token);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenMalformed() {
        String badToken = "not.a.jwt.token";

        assertFalse(jwtService.isTokenValid(badToken));
    }

    @Test
    void getTokenType_shouldReturnNull_whenTokenMalformed() {
        String badToken = "not.a.jwt.token";

        assertNull(jwtService.getTokenType(badToken));
    }
}