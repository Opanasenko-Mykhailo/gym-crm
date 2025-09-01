package com.gcm.repository;

import com.gcm.model.TrainerSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerSummaryRepository extends JpaRepository<TrainerSummary, Long> {
    Optional<TrainerSummary> findByUsername(String username);
}
