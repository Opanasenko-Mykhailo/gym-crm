package com.gcc.app.service.integration.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerSummaryResponseDto {
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<YearlySummaryDto> years;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlySummaryDto {
        private int yearNumber;
        private List<MonthlySummaryDto> months;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummaryDto {
        private int monthNumber;
        private int totalDurationMinutes;
    }
}