package com.gcm.mapper;

import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.YearlySummaryResponse;
import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface TrainerSummaryMapper {

    TrainerSummaryResponse toRestModel(TrainerSummary trainerSummary);

    default List<YearlySummaryResponse> mapYears(List<YearlySummary> years) {
        return Optional.ofNullable(years).orElse(List.of()).stream()
                .map(y -> new YearlySummaryResponse(
                        y.getYearNumber(),
                        Optional.ofNullable(y.getMonths())
                                .orElse(List.of())
                                .stream()
                                .mapToInt(MonthlySummary::getTotalDurationMinutes)
                                .sum()))
                .toList();
    }
}