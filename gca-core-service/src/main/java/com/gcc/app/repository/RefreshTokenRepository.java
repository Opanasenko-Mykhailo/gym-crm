package com.gcc.app.repository;

import com.gcc.app.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String tokenHash);

    void deleteByToken(String tokenHash);

    int deleteAllByExpiryDateBefore(Instant now);
}