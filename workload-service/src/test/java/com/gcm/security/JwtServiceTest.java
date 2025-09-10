package com.gcm.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = "jwt.secret=my-super-secret-key-which-is-long-enough-for-hmacsha")
class JwtServiceTest {

    private static final String TEST_USERNAME = "alice.smith";
    private static final String TEST_SECRET = "my-super-secret-key-which-is-long-enough-for-hmacsha";
    private static final String INVALID_TOKEN = "invalid.token.here";

    @Autowired
    private JwtService service;

    @Test
    void givenValidToken_whenIsTokenValid_thenReturnsTrue() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        boolean isActual = service.isTokenValid(token);

        assertThat(isActual).isTrue();
    }

    @Test
    void givenExpiredToken_whenIsTokenValid_thenReturnsFalse() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        boolean valid = service.isTokenValid(token);

        assertThat(valid).isFalse();
    }

    @Test
    void givenToken_whenExtractUsername_thenReturnsCorrectUsername() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        String username = service.extractUsername(token);

        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    void givenTokenWithRoles_whenGetAuthorities_thenReturnsListOfGrantedAuthorities() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes()))
                .compact();

        List<SimpleGrantedAuthority> authorities = service.getAuthorities(token);

        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void givenInvalidToken_whenExtractUsername_thenThrowsException() {
        assertThrows(Exception.class, () -> service.extractUsername(INVALID_TOKEN));
    }

    @Test
    void givenInvalidToken_whenGetAuthorities_thenThrowsException() {
        assertThrows(Exception.class, () -> service.getAuthorities(INVALID_TOKEN));
    }

    @Test
    void givenInvalidToken_whenIsTokenActual_thenReturnsFalse() {
        assertThat(service.isTokenValid(INVALID_TOKEN)).isFalse();
    }
}