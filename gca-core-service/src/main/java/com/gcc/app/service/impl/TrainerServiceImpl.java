package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TrainerCreateRequestDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TrainerUpdateRequestDto;
import com.gcc.app.mapper.TrainerMapper;
import com.gcc.app.model.Role;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import com.gcc.app.model.TrainingType;
import com.gcc.app.model.User;
import com.gcc.app.repository.TrainerRepository;
import com.gcc.app.repository.TrainingQueryRepository;
import com.gcc.app.service.RoleService;
import com.gcc.app.service.TrainerService;
import com.gcc.app.service.TrainingTypeService;
import com.gcc.app.service.UserService;
import com.gcc.app.service.common.CredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

import static com.gcc.app.model.enums.RoleType.ROLE_TRAINER;
import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TrainerServiceImpl implements TrainerService {

    private static final String TRAINER_NOT_FOUND_MSG = "Trainer not found with username: %s";

    private final TrainerRepository trainerRepository;
    private final TrainingQueryRepository trainingQueryRepository;
    private final UserService userService;
    private final TrainingTypeService trainingTypeService;
    private final CredentialsService credentialsService;
    private final RoleService roleService;
    private final TrainerMapper trainerMapper;

    @Transactional
    @Override
    public Trainer createTrainer(@Valid TrainerCreateRequestDto trainerCreateRequestDto) {
        TrainingType specialization = getSpecialization(trainerCreateRequestDto.getSpecialization());

        Trainer trainer = trainerMapper.toEntity(trainerCreateRequestDto);
        User user = trainer.getUser();
        log.info("Creating trainer: {} {}", user.getFirstName(), user.getLastName());

        String username = credentialsService.generateUsername(user.getFirstName(), user.getLastName(), userService.getAllUsernames());
        String password = credentialsService.generateRandomPassword();
        String encryptedPassword = credentialsService.encodePassword(password);

        Trainer trainerWithCredentials = trainer.toBuilder()
                .user(userWithCredentials(user, username, encryptedPassword))
                .specialization(specialization)
                .build();

        Trainer createdTrainer = trainerRepository.save(trainerWithCredentials);
        log.info("Trainer created with username: {}", username);

        return createdTrainer.toBuilder()
                .user(userWithCredentials(user, username, password))
                .build();
    }

    @Transactional
    @Override
    public Trainer updateTrainer(@Valid TrainerUpdateRequestDto dto) {
        String username = dto.getUsername();
        Trainer existing = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINER_NOT_FOUND_MSG, username)));

        Trainer updated = buildUpdatedTrainer(existing, dto);

        return trainerRepository.save(updated);
    }

    @Transactional(readOnly = true)
    @Override
    public Trainer getByUsername(String username) {
        log.info("Getting trainer by username: {}", username);

        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINER_NOT_FOUND_MSG, username)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Training> getTrainerTrainings(@Valid TrainerTrainingSearchCriteriaDto criteria) {
        log.info("Searching trainings with criteria: {}", criteria);

        return trainingQueryRepository.findTrainingsForTrainer(criteria);
    }

    @Transactional
    @Override
    public void setTrainerActivationStatus(String username, boolean isActive) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException(String.format(TRAINER_NOT_FOUND_MSG, username)));

        User updatedUser = trainer.getUser().toBuilder()
                .isActive(isActive)
                .build();
        Trainer updatedTrainer = trainer.toBuilder()
                .user(updatedUser)
                .build();

        trainerRepository.save(updatedTrainer);

        log.info("Trainer {} set to {}", username, isActive ? "active" : "inactive");
    }

    @Transactional(readOnly = true)
    @Override
    public List<Trainer> getUnassignedForTrainee(Trainee trainee) {
        return trainerRepository.findAllNotAssignedToTrainee(trainee);
    }

    private User userWithCredentials(User user, String username, String password) {
        Role trainerRole = roleService.getByType(ROLE_TRAINER);

        return User.builder()
                .username(username)
                .password(password)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(true)
                .roles(Set.of(trainerRole))
                .build();
    }

    private Trainer buildUpdatedTrainer(Trainer trainer, TrainerUpdateRequestDto dto) {
        User.UserBuilder userBuilder = trainer.getUser().toBuilder();
        ofNullable(dto.getFirstName())
                .ifPresent(userBuilder::firstName);
        ofNullable(dto.getLastName())
                .ifPresent(userBuilder::lastName);
        ofNullable(dto.getUsername())
                .ifPresent(userBuilder::username);
        ofNullable(dto.getIsActive())
                .ifPresent(userBuilder::isActive);

        Trainer.TrainerBuilder trainerBuilder = trainer.toBuilder()
                .user(userBuilder.build());
        ofNullable(dto.getSpecialization())
                .ifPresent(specialization -> trainerBuilder.specialization(getSpecialization(specialization)));

        return trainerBuilder.build();
    }

    private TrainingType getSpecialization(TrainingType specialization) {
        if (specialization == null || specialization.getName() == null) {
            throw new ServiceException("Specialization cannot be null");
        }

        return trainingTypeService.getByName(specialization.getName());
    }
}