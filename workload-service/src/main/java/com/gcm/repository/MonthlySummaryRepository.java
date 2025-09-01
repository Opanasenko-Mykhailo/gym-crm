package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
}