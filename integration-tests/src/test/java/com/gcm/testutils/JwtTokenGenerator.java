package com.gcm.testutils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenGenerator {

    private static SecretKey key;

    @Value("${jwt.secret}")
    private void setSecret(String secret) {
        JwtTokenGenerator.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String generateToken(String username) {
        Map<String, Object> claims = Map.of(
                "roles", List.of("ROLE_TRAINER"),
                "type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
    }
}