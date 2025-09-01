package com.gcm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "yearly_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_number", nullable = false)
    @NotNull
    @Min(2000)
    private Integer yearNumber;

    @OneToMany(mappedBy = "yearlySummary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlySummary> months;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_summary_id")
    private TrainerSummary trainerSummary;
}