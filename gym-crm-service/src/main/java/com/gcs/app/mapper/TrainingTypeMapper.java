package com.gcs.app.mapper;

import com.gcs.app.facade.dto.TrainingTypeResponseDto;
import com.gcs.app.model.TrainingType;
import com.gcs.app.rest.TrainingTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeResponseDto toDto(TrainingType trainingType);

    List<TrainingTypeResponseDto> toDtoList(List<TrainingType> trainingTypes);

    @Mapping(source = "name", target = "trainingType")
    @Mapping(source = "id", target = "trainingTypeId")
    TrainingTypeResponse toRestModel(TrainingTypeResponseDto dto);

    List<TrainingTypeResponse> toRestModelList(List<TrainingTypeResponseDto> dtoList);
}
