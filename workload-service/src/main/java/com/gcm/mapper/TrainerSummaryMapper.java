package com.gcm.mapper;

import com.gcm.app.rest.MonthlySummaryResponse;
import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.YearlySummaryResponse;
import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.gcm.service.dto.TrainerSummaryResponseDto;
import com.gcm.service.dto.TrainerSummaryResponseDto.MonthlySummaryDto;
import com.gcm.service.dto.TrainerSummaryResponseDto.YearlySummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface TrainerSummaryMapper {

    TrainerSummaryResponseDto toDto(TrainerSummary trainerSummary);

    @Mapping(target = "years", source = "years")
    TrainerSummaryResponse toRestModel(TrainerSummaryResponseDto dto);

    @Mapping(target = "year", source = "yearNumber")
    @Mapping(target = "months", source = "months")
    YearlySummaryResponse toRestYear(YearlySummaryDto dto);

    @Mapping(target = "month", source = "monthNumber")
    @Mapping(target = "duration", source = "totalDurationMinutes")
    MonthlySummaryResponse toRestMonth(MonthlySummaryDto dto);

    default List<YearlySummaryDto> mapYears(List<YearlySummary> years) {
        return Optional.ofNullable(years).orElse(List.of()).stream()
                .map(y -> new YearlySummaryDto(
                        y.getYearNumber(),
                        Optional.ofNullable(y.getMonths())
                                .orElse(List.of())
                                .stream()
                                .mapToInt(MonthlySummary::getTotalDurationMinutes)
                                .sum(),
                        mapMonths(y.getMonths())
                ))
                .toList();
    }

    default List<MonthlySummaryDto> mapMonths(List<MonthlySummary> months) {
        return Optional.ofNullable(months).orElse(List.of()).stream()
                .map(m -> new MonthlySummaryDto(
                        m.getMonthNumber(),
                        m.getTotalDurationMinutes()
                ))
                .toList();
    }
}