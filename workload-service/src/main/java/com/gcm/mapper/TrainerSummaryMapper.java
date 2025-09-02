package com.gcm.mapper;

import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.model.TrainerSummary;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainerSummaryMapper {
    TrainerSummaryRequest toRestModel(TrainerSummary trainerSummary);
}
