package com.gcs.app.mapper;

import com.gcs.app.facade.dto.TrainingCreateRequestDto;
import com.gcs.app.facade.dto.TrainingResponseDto;
import com.gcs.app.model.Training;
import com.gcs.app.model.TrainingType;
import com.gcs.app.rest.TraineeTrainingGetResponse;
import com.gcs.app.rest.TrainerTrainingGetResponse;
import com.gcs.app.rest.TrainingCreateRequest;
import com.gcs.app.rest.TrainingTypeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    Training toEntity(TrainingCreateRequestDto dto);

    TrainingResponseDto toDto(Training training);

    @Mapping(source = "name", target = "trainingName")
    @Mapping(source = "date", target = "trainingDate")
    @Mapping(source = "duration", target = "trainingDuration")
    @Mapping(source = "type.name", target = "trainingType")
    @Mapping(source = "trainee.user.username", target = "traineeName")
    TrainerTrainingGetResponse toTrainerTrainingRestModel(Training training);

    @Mapping(source = "name", target = "trainingName")
    @Mapping(source = "date", target = "trainingDate")
    @Mapping(source = "duration", target = "trainingDuration")
    @Mapping(source = "type.name", target = "trainingType")
    @Mapping(source = "trainer.user.username", target = "trainerName")
    TraineeTrainingGetResponse toTraineeTrainingRestModel(Training training);

    @Mapping(source = "trainingName", target = "name")
    @Mapping(source = "trainingDate", target = "date")
    @Mapping(source = "trainingDuration", target = "duration")
    @Mapping(source = "trainingTypeName", target = "type.name")
    TrainingCreateRequestDto toTrainingCreateRequestDto(TrainingCreateRequest trainingCreateRequest);

    @Mapping(source = "name", target = "trainingType")
    @Mapping(source = "id", target = "trainingTypeId")
    TrainingTypeResponse toTrainingTypeRestModel(TrainingType trainingType);
}
