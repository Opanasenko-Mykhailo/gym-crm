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

    @Autowired
    private JwtService jwtService;

    private static final String TEST_USERNAME = "alice.smith";

    @Test
    void givenValidToken_whenIsTokenValid_thenReturnsTrue() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("my-super-secret-key-which-is-long-enough-for-hmacsha".getBytes()))
                .compact();

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void givenExpiredToken_whenIsTokenValid_thenReturnsFalse() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(Keys.hmacShaKeyFor("my-super-secret-key-which-is-long-enough-for-hmacsha".getBytes()))
                .compact();

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isFalse();
    }

    @Test
    void givenToken_whenExtractUsername_thenReturnsCorrectUsername() {
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("my-super-secret-key-which-is-long-enough-for-hmacsha".getBytes()))
                .compact();

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    void givenTokenWithRoles_whenGetAuthorities_thenReturnsListOfGrantedAuthorities() {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .claim("roles", roles)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("my-super-secret-key-which-is-long-enough-for-hmacsha".getBytes()))
                .compact();

        List<SimpleGrantedAuthority> authorities = jwtService.getAuthorities(token);

        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void givenInvalidToken_whenParse_thenThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
        assertThrows(Exception.class, () -> jwtService.getAuthorities(invalidToken));
        assertThat(jwtService.isTokenValid(invalidToken)).isFalse();
    }
}