package com.gcm.repository;

import com.gcm.model.TrainerSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerSummaryRepository extends MongoRepository<TrainerSummary, String> {
    Optional<TrainerSummary> findByUsername(String username);
}