package com.gcc.app.mapper;

import com.gcc.app.facade.dto.TrainingTypeResponseDto;
import com.gcc.app.model.TrainingType;
import com.gcc.app.rest.TrainingTypeResponse;
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
