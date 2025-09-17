package com.gcc.app.facade;

import com.gcc.app.facade.dto.AuthRequestDto;
import com.gcc.app.facade.dto.AuthResponseDto;
import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.facade.dto.RefreshTokenRequestDto;
import com.gcc.app.facade.dto.TraineeCreateRequestDto;
import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TraineeUpdateRequestDto;
import com.gcc.app.facade.dto.TrainerCreateRequestDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TrainerUpdateRequestDto;
import com.gcc.app.facade.dto.TrainingCreateRequestDto;
import com.gcc.app.facade.dto.TrainingResponseDto;
import com.gcc.app.facade.dto.TrainingTypeResponseDto;
import com.gcc.app.integration.workload.WorkloadService;
import com.gcc.app.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto;
import com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto.ActionType;
import com.gcc.app.mapper.TraineeMapper;
import com.gcc.app.mapper.TrainerMapper;
import com.gcc.app.mapper.TrainingMapper;
import com.gcc.app.mapper.TrainingTypeMapper;
import com.gcc.app.mapper.UserMapper;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import com.gcc.app.rest.AvailableTrainerGetResponse;
import com.gcc.app.rest.ChangePasswordRequest;
import com.gcc.app.rest.LoginRequest;
import com.gcc.app.rest.LoginResponse;
import com.gcc.app.rest.RefreshTokenRequest;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateResponse;
import com.gcc.app.rest.TraineeCreateRequest;
import com.gcc.app.rest.TraineeGetResponse;
import com.gcc.app.rest.TraineeTrainingGetResponse;
import com.gcc.app.rest.TraineeUpdateRequest;
import com.gcc.app.rest.TraineeUpdateResponse;
import com.gcc.app.rest.TrainerCreateRequest;
import com.gcc.app.rest.TrainerGetResponse;
import com.gcc.app.rest.TrainerTrainingGetResponse;
import com.gcc.app.rest.TrainerUpdateRequest;
import com.gcc.app.rest.TrainerUpdateResponse;
import com.gcc.app.rest.TrainingCreateRequest;
import com.gcc.app.rest.TrainingTypeResponse;
import com.gcc.app.rest.UserCreationResponse;
import com.gcc.app.security.AuthService;
import com.gcc.app.service.TraineeService;
import com.gcc.app.service.TrainerService;
import com.gcc.app.service.TrainingService;
import com.gcc.app.service.TrainingTypeService;
import com.gcc.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto.ActionType.ADD;
import static com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto.ActionType.DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final UserService userService;
    private final AuthService authService;
    private final WorkloadService workloadService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final TrainingTypeMapper trainingTypeMapper;
    private final UserMapper userMapper;

    public UserCreationResponse createTrainee(TraineeCreateRequest request) {
        log.info("Creating trainee: {} {}", request.getFirstName(), request.getLastName());
        TraineeCreateRequestDto createRequestDto = traineeMapper.toCreateRequestDto(request);

        Trainee saved = traineeService.createTrainee(createRequestDto);

        UserCreationResponse authResponse = new UserCreationResponse();
        authResponse.setUsername(saved.getUser().getUsername());
        authResponse.setPassword(saved.getUser().getPassword());

        return authResponse;
    }

    public TraineeUpdateResponse updateTrainee(TraineeUpdateRequest request, String username) {
        log.info("Updating trainee with username: {}", username);
        TraineeUpdateRequestDto updateRequestDto = traineeMapper.toUpdateRequestDto(request);
        updateRequestDto.setUsername(username);

        return traineeMapper.toUpdateRestModel(traineeService.updateTrainee(updateRequestDto));
    }

    public void deleteTraineeByUsername(String username) {
        log.info("Deleting trainee with username: {}", username);
        traineeService.deleteTraineeByUsername(username);

        notifyOnWorkloadRemoval(username);
    }

    public TraineeGetResponse getTraineeByUsername(String username) {
        log.info("Retrieving trainee by username: {}", username);
        Trainee trainee = traineeService.getByUsername(username);

        return traineeMapper.toRestModel(trainee);
    }

    public List<TraineeTrainingGetResponse> getTraineeTrainings(TraineeTrainingSearchCriteriaDto criteria) {
        log.info("Getting trainee trainings with criteria: {}", criteria);
        var trainings = traineeService.getTraineeTrainings(criteria);

        return trainings.stream()
                .map(trainingMapper::toTraineeTrainingRestModel)
                .toList();
    }

    public TraineeAssignedTrainersUpdateResponse updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        log.info("Updating trainers for trainee: {}", traineeUsername);
        Trainee updated = traineeService.updateTraineeTrainers(traineeUsername, trainerUsernames);

        return traineeMapper.toAssignedTrainersRestModel(updated);
    }

    public void setTraineeActive(String username, boolean isActive) {
        log.info("Setting trainee {} to {}", username, isActive ? "active" : "inactive");

        traineeService.setTraineeActivationStatus(username, isActive);
    }

    public UserCreationResponse createTrainer(TrainerCreateRequest request) {
        log.info("Creating trainer: {} {}", request.getFirstName(), request.getLastName());
        TrainerCreateRequestDto createRequestDto = trainerMapper.toCreateRequestDto(request);

        Trainer saved = trainerService.createTrainer(createRequestDto);

        UserCreationResponse authResponse = new UserCreationResponse();
        authResponse.setUsername(saved.getUser().getUsername());
        authResponse.setPassword(saved.getUser().getPassword());

        return authResponse;
    }

    public TrainerUpdateResponse updateTrainer(TrainerUpdateRequest request, String username) {
        log.info("Updating trainer with username: {}", username);
        TrainerUpdateRequestDto updateRequestDto = trainerMapper.toUpdateRequestDto(request);
        updateRequestDto.setUsername(username);

        return trainerMapper.toUpdateRestModel(trainerService.updateTrainer(updateRequestDto));
    }

    public TrainerGetResponse getTrainerByUsername(String username) {
        log.info("Retrieving trainer by username: {}", username);
        Trainer trainer = trainerService.getByUsername(username);

        return trainerMapper.toRestModel(trainer);
    }

    public void createTraining(TrainingCreateRequest request) {
        log.info("Creating training: {}", request.getTrainingName());
        TrainingCreateRequestDto createRequestDto = trainingMapper.toTrainingCreateRequestDto(request);

        Training training = trainingService.createTraining(createRequestDto);

        workloadService.notifyWorkloadChange(buildWorkloadRequest(training, ADD));
    }

    public TrainingResponseDto getTraining(Long id) {
        log.info("Retrieving training with id: {}", id);
        Training training = trainingService.getTraining(id);

        return trainingMapper.toDto(training);
    }

    public List<TrainerTrainingGetResponse> getTrainerTrainings(TrainerTrainingSearchCriteriaDto criteria) {
        log.info("Getting trainer trainings with criteria: {}", criteria);
        var trainings = trainerService.getTrainerTrainings(criteria);

        return trainings.stream()
                .map(trainingMapper::toTrainerTrainingRestModel)
                .toList();
    }

    public List<TrainingTypeResponse> getAllTrainingTypes() {
        List<TrainingTypeResponseDto> trainingTypeResponseDtoList = trainingTypeService.getAll();
        log.info("Retrieved {} training types", trainingTypeResponseDtoList.size());

        return trainingTypeMapper.toRestModelList(trainingTypeResponseDtoList);
    }

    public void setTrainerActive(String username, boolean isActive) {
        log.info("Setting trainer {} to {}", username, isActive ? "active" : "inactive");

        trainerService.setTrainerActivationStatus(username, isActive);
    }

    public List<AvailableTrainerGetResponse> getUnassignedTrainers(String traineeUsername) {
        log.info("Getting unassigned trainers for trainee: {}", traineeUsername);
        List<Trainer> trainers = traineeService.getUnassignedTrainers(traineeUsername);

        return trainers.stream()
                .map(trainerMapper::toAvailableTrainerRestModel)
                .toList();
    }

    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for username: {}", request.getUsername());
        PasswordChangeRequestDto dto = userMapper.toPasswordChangeRequestDto(request);

        userService.changePassword(dto);
    }

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        AuthRequestDto dto = userMapper.toAuthRequestDto(request);

        return userMapper.toLoginResponse(authService.authenticate(dto));
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenRequestDto dto = userMapper.toRefreshTokenRequestDto(request);
        AuthResponseDto response = authService.refreshToken(dto);
        log.info("Token refreshed successfully");

        return userMapper.toLoginResponse(response);
    }

    public void logout(RefreshTokenRequest request) {
        authService.logout(userMapper.toLogoutRequestDto(request));
        log.info("User logged out successfully");
    }

    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        log.info("Retrieving trainer summary for username: {}", username);

        return workloadService.getTrainerSummary(username);
    }

    private void notifyOnWorkloadRemoval(String traineeUsername) {
        TraineeTrainingSearchCriteriaDto criteria = new TraineeTrainingSearchCriteriaDto();
        criteria.setUsername(traineeUsername);

        traineeService.getTraineeTrainings(criteria)
                .forEach(training -> workloadService.notifyWorkloadChange(
                        buildWorkloadRequest(training, DELETE)));
    }

    private TrainerWorkloadRequestDto buildWorkloadRequest(Training training, ActionType actionType) {
        TrainerWorkloadRequestDto request = new TrainerWorkloadRequestDto();
        request.setUsername(training.getTrainer().getUser().getUsername());
        request.setFirstName(training.getTrainer().getUser().getFirstName());
        request.setLastName(training.getTrainer().getUser().getLastName());
        request.setTrainingDate(training.getDate());
        request.setActive(true);
        request.setDurationInMinutes(training.getDuration());
        request.setActionType(actionType);

        return request;
    }
}