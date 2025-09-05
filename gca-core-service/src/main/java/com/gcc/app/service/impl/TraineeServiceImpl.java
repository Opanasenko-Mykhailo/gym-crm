package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TraineeCreateRequestDto;
import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TraineeUpdateRequestDto;
import com.gcc.app.mapper.TraineeMapper;
import com.gcc.app.model.Role;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import com.gcc.app.model.User;
import com.gcc.app.repository.TraineeRepository;
import com.gcc.app.repository.TrainingQueryRepository;
import com.gcc.app.service.RoleService;
import com.gcc.app.service.TraineeService;
import com.gcc.app.service.TrainerService;
import com.gcc.app.service.UserService;
import com.gcc.app.service.common.CredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gcc.app.model.enums.RoleType.ROLE_TRAINEE;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TraineeServiceImpl implements TraineeService {

    private static final String TRAINEE_NOT_FOUND_MSG = "Trainee not found with username: %s";

    private final TraineeRepository traineeRepository;
    private final TrainingQueryRepository trainingQueryRepository;
    private final UserService userService;
    private final TrainerService trainerService;
    private final RoleService roleService;
    private final CredentialsService credentialsService;
    private final TraineeMapper traineeMapper;

    @Transactional
    @Override
    public Trainee createTrainee(@Valid TraineeCreateRequestDto requestDto) {
        Trainee trainee = traineeMapper.toEntity(requestDto);
        User user = trainee.getUser();
        log.info("Creating trainee: {} {}", user.getFirstName(), user.getLastName());

        String username = credentialsService.generateUsername(user.getFirstName(), user.getLastName(), userService.getAllUsernames());
        String password = credentialsService.generateRandomPassword();
        String encryptedPassword = credentialsService.encodePassword(password);


        Trainee traineeWithCredentials = trainee.toBuilder()
                .user(userWithCredentials(user, username, encryptedPassword))
                .build();

        Trainee createdTrainee = traineeRepository.save(traineeWithCredentials);
        log.info("Trainee created with username: {}", username);

        return createdTrainee.toBuilder()
                .user(userWithCredentials(user, username, password))
                .build();
    }

    @Transactional
    @Override
    public Trainee updateTrainee(@Valid TraineeUpdateRequestDto dto) {
        String username = dto.getUsername();
        Trainee existing = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, username)));

        Trainee updated = buildUpdatedTrainee(existing, dto);

        return traineeRepository.save(updated);
    }

    @Transactional
    @Override
    public void deleteTraineeByUsername(String username) {
        log.info("Deleting trainee with username: {}", username);

        traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, username)));

        traineeRepository.deleteByUser_Username(username);
        log.debug("Trainee with username {} deleted", username);
    }

    @Transactional(readOnly = true)
    @Override
    public Trainee getByUsername(String username) {
        log.info("Getting trainee by username: {}", username);

        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, username)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Training> getTraineeTrainings(@Valid TraineeTrainingSearchCriteriaDto criteria) {
        log.info("Searching trainings with criteria: {}", criteria);

        return trainingQueryRepository.findTrainingsForTrainee(criteria);
    }

    @Transactional(readOnly = true)
    @Override
    public void setTraineeActivationStatus(String username, boolean isActive) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, username)));

        User updatedUser = trainee.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainee updatedTrainee = trainee.toBuilder()
                .user(updatedUser)
                .build();

        traineeRepository.save(updatedTrainee);
        log.info("Trainee {} set to {}", username, isActive ? "active" : "inactive");
    }

    @Transactional(readOnly = true)
    @Override
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, traineeUsername)));

        return trainerService.getUnassignedForTrainee(trainee);
    }

    @Transactional
    @Override
    public Trainee updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new ServiceException(String.format(TRAINEE_NOT_FOUND_MSG, traineeUsername)));

        Set<Trainer> newTrainers = trainerUsernames.stream()
                .map(trainerService::getByUsername)
                .collect(Collectors.toSet());

        setTraineeTrainers(trainee, newTrainers);

        return traineeRepository.save(trainee);
    }

    private User userWithCredentials(User user, String username, String password) {
        Role traineeRole = roleService.getByType(ROLE_TRAINEE);

        return User.builder()
                .username(username)
                .password(password)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(true)
                .roles(Set.of(traineeRole))
                .build();
    }

    private Trainee buildUpdatedTrainee(Trainee trainee, TraineeUpdateRequestDto dto) {
        User.UserBuilder userBuilder = trainee.getUser().toBuilder();
        ofNullable(dto.getFirstName())
                .ifPresent(userBuilder::firstName);
        ofNullable(dto.getLastName())
                .ifPresent(userBuilder::lastName);
        ofNullable(dto.getUsername())
                .ifPresent(userBuilder::username);
        ofNullable(dto.getIsActive())
                .ifPresent(userBuilder::isActive);

        Trainee.TraineeBuilder traineeBuilder = trainee.toBuilder()
                .user(userBuilder.build())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress());

        return traineeBuilder.build();
    }

    private void addTrainerToTrainee(Trainee trainee, Trainer trainer) {
        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
        }

        if (!trainer.getTrainees().contains(trainee)) {
            trainer.getTrainees().add(trainee);
        }
    }

    private void removeTrainerFromTrainee(Trainee trainee, Trainer trainer) {
        trainee.getTrainers().remove(trainer);
        trainer.getTrainees().remove(trainee);
    }

    private void setTraineeTrainers(Trainee trainee, Set<Trainer> newTrainers) {
        new HashSet<>(trainee.getTrainers())
                .forEach(oldTrainer -> removeTrainerFromTrainee(trainee, oldTrainer));

        newTrainers.forEach(trainer -> addTrainerToTrainee(trainee, trainer));
    }
}