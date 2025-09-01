package com.gcs.app.security;

import com.gcs.app.exception.RefreshTokenNotFoundException;
import com.gcs.app.model.RefreshTokenEntity;
import com.gcs.app.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final TokenHasher tokenHasher;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpirationTime;

    @Transactional
    public void saveToken(String token, String username) {
        String hashed = tokenHasher.hash(token);
        Instant expiry = Instant.now().plusMillis(refreshExpirationTime);

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .token(hashed)
                .username(username)
                .expiryDate(expiry)
                .build();

        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public String getUsername(String token) {
        String hashed = tokenHasher.hash(token);

        return repository.findByToken(hashed)
                .map(RefreshTokenEntity::getUsername)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Token not found"));
    }

    @Transactional
    public void invalidateToken(String token) {
        String hashed = tokenHasher.hash(token);
        repository.deleteByToken(hashed);
    }
}