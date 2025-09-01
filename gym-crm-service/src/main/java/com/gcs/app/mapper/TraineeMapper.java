package com.gcs.app.mapper;

import com.gcs.app.facade.dto.TraineeCreateRequestDto;
import com.gcs.app.facade.dto.TraineeResponseDto;
import com.gcs.app.facade.dto.TraineeUpdateRequestDto;
import com.gcs.app.model.Trainee;
import com.gcs.app.rest.TraineeAssignedTrainersUpdateResponse;
import com.gcs.app.rest.TraineeCreateRequest;
import com.gcs.app.rest.TraineeGetResponse;
import com.gcs.app.rest.TraineeUpdateRequest;
import com.gcs.app.rest.TraineeUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TrainerMapper.class})
public interface TraineeMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    Trainee toEntity(TraineeCreateRequestDto dto);

    TraineeResponseDto toDto(Trainee trainee);

    TraineeCreateRequestDto toCreateRequestDto(TraineeCreateRequest request);

    TraineeUpdateRequestDto toUpdateRequestDto(TraineeUpdateRequest request);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "isActive")
    @Mapping(source = "trainers", target = "trainers")
    TraineeGetResponse toRestModel(Trainee trainee);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "isActive")
    TraineeUpdateResponse toUpdateRestModel(Trainee trainee);

    @Mapping(source = "trainers", target = "trainers")
    TraineeAssignedTrainersUpdateResponse toAssignedTrainersRestModel(Trainee trainee);
}
