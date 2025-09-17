package com.gcm.mapper;


import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {
    TrainerWorkloadRequestDto toDto(TrainerWorkloadRequest request);

    TrainerWorkloadRequest toRestModel(TrainerWorkloadRequestDto dto);
}