package com.gcs.app.mapper;

import com.gcs.app.facade.dto.TrainerCreateRequestDto;
import com.gcs.app.facade.dto.TrainerResponseDto;
import com.gcs.app.facade.dto.TrainerUpdateRequestDto;
import com.gcs.app.model.Trainer;
import com.gcs.app.rest.AvailableTrainerGetResponse;
import com.gcs.app.rest.TraineeAssignedTrainersUpdateResponseTrainersInner;
import com.gcs.app.rest.TraineeGetResponseAllOfTrainers;
import com.gcs.app.rest.TraineeUpdateResponseAllOfTrainers;
import com.gcs.app.rest.TrainerCreateRequest;
import com.gcs.app.rest.TrainerGetResponse;
import com.gcs.app.rest.TrainerUpdateRequest;
import com.gcs.app.rest.TrainerUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Trainer toEntity(TrainerCreateRequestDto dto);

    TrainerResponseDto toDto(Trainer trainer);

    @Mapping(target = "specialization.name", source = "specialization")
    TrainerCreateRequestDto toCreateRequestDto(TrainerCreateRequest request);

    @Mapping(target = "specialization.name", source = "specialization")
    TrainerUpdateRequestDto toUpdateRequestDto(TrainerUpdateRequest request);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "specialization.name", target = "specialization")
    TrainerGetResponse toRestModel(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "specialization.name", target = "specialization")
    TrainerUpdateResponse toUpdateRestModel(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "specialization.name", target = "specialization")
    AvailableTrainerGetResponse toAvailableTrainerRestModel(Trainer trainer);

    @Mapping(source = "specialization.name", target = "specialization")
    TraineeGetResponseAllOfTrainers toTraineeTrainerRestModel(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "specialization.name", target = "specialization")
    TraineeUpdateResponseAllOfTrainers toTraineeUpdateTrainerRestModel(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "specialization.name", target = "specialization")
    TraineeAssignedTrainersUpdateResponseTrainersInner toTraineeAssignedTrainerInnerRestModel(Trainer trainer);
}