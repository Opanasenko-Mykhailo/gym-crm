package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import com.gcm.model.YearlySummary;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataSet("datasets/monthly_summary.yml")
class MonthlySummaryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private MonthlySummaryRepository repository;

    @Autowired
    private YearlySummaryRepository yearlySummaryRepository;

    @Test
    void findAll_shouldReturnAllMonthlySummaries() {
        List<MonthlySummary> summaries = repository.findAll();
        assertThat(summaries).isNotEmpty();
    }

    @Test
    @DataSet(cleanBefore = true)
    void save_shouldPersistMonthlySummary_withYearlySummary() {
        YearlySummary yearly = YearlySummary.builder()
                .yearNumber(2025)
                .build();
        yearly = yearlySummaryRepository.save(yearly);

        MonthlySummary summary = MonthlySummary.builder()
                .monthNumber(3)
                .totalDurationMinutes(120)
                .yearlySummary(yearly)
                .build();

        MonthlySummary saved = repository.save(summary);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getYearlySummary()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }
}