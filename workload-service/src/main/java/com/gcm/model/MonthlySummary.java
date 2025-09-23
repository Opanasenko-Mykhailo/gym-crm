package com.gcm.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummary {

    @NotNull
    @Min(1)
    @Max(12)
    private Integer monthNumber;

    @NotNull
    @Min(0)
    private Integer totalDurationMinutes;
}