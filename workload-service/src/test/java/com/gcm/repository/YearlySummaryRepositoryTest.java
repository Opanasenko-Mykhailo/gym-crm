package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataSet("datasets/yearly_summary.yml")
class YearlySummaryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private YearlySummaryRepository repository;

    @Autowired
    private TrainerSummaryRepository trainerSummaryRepository;

    @Test
    void findAll_shouldReturnAllYearlySummaries() {
        List<YearlySummary> summaries = repository.findAll();
        assertThat(summaries).isNotEmpty();
    }

    @Test
    @DataSet(cleanBefore = true)
    void save_shouldPersistYearlySummary_withTrainer_andMonths() {
        TrainerSummary trainer = TrainerSummary.builder()
                .username("bob.builder")
                .firstName("Bob")
                .lastName("Builder")
                .active(true)
                .build();
        trainer = trainerSummaryRepository.save(trainer);

        MonthlySummary month = MonthlySummary.builder()
                .monthNumber(5)
                .totalDurationMinutes(100)
                .build();

        YearlySummary yearly = YearlySummary.builder()
                .yearNumber(2025)
                .trainerSummary(trainer)
                .months(List.of(month))
                .build();

        month.setYearlySummary(yearly);

        YearlySummary saved = repository.save(yearly);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTrainerSummary()).isNotNull();
        assertThat(saved.getMonths()).hasSize(1);
    }
}