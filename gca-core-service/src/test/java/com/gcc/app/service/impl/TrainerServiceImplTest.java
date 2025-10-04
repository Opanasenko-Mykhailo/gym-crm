package com.gcc.app.service.impl;

import com.gcc.app.exception.EntityNotFoundException;
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
import com.gcc.app.service.TrainingTypeService;
import com.gcc.app.service.UserService;
import com.gcc.app.service.common.CredentialsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.gcc.app.model.enums.RoleType.ROLE_TRAINER;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String UPDATED_FIRST_NAME = "Janet";
    private static final String UPDATED_LAST_NAME = "Johnson";
    private static final String USERNAME = "jane.smith";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$encodedPass";
    private static final String SPECIALIZATION = "Yoga";
    private static final String TRAINER_NOT_FOUND_MESSAGE = format("Trainer not found with username: %s", USERNAME);
    private static final Trainer TRAINER = createTrainer();
    private static final TrainerCreateRequestDto CREATE_REQUEST = createTrainerCreateRequestDto();
    private static final TrainerUpdateRequestDto UPDATE_REQUEST = createTrainerUpdateRequestDto();

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingQueryRepository trainingQueryRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private UserService userService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private TrainerServiceImpl service;

    @Test
    void createTrainer_mapsDtoAndCreatesTrainer_returnsTrainer() {
        when(roleService.getByType(ROLE_TRAINER)).thenReturn(new Role(1L, ROLE_TRAINER));
        when(trainerMapper.toEntity(CREATE_REQUEST)).thenReturn(TRAINER);
        when(userService.getAllUsernames()).thenReturn(Collections.emptySet());
        when(credentialsService.generateUsername(FIRST_NAME, LAST_NAME, Collections.emptySet())).thenReturn(USERNAME);
        when(credentialsService.generateRandomPassword()).thenReturn(PASSWORD);
        when(credentialsService.encodePassword(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeService.getByName(SPECIALIZATION)).thenReturn(createTrainingType());
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer actual = service.createTrainer(CREATE_REQUEST);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(PASSWORD, actual.getUser().getPassword());
        assertTrue(actual.getUser().isActive());
        assertEquals(SPECIALIZATION, actual.getSpecialization().getName());

        verify(trainerMapper).toEntity(CREATE_REQUEST);
        verify(userService).getAllUsernames();
        verify(credentialsService).generateUsername(FIRST_NAME, LAST_NAME, Collections.emptySet());
        verify(credentialsService).generateRandomPassword();
        verify(credentialsService).encodePassword(PASSWORD);
        verify(trainingTypeService).getByName(SPECIALIZATION);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_whenTrainerExists_updatesAndReturnsTrainer() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINER));
        when(trainingTypeService.getByName(SPECIALIZATION)).thenReturn(createTrainingType());
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer actual = service.updateTrainer(UPDATE_REQUEST);

        assertEquals(UPDATED_FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(UPDATED_LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertTrue(actual.getUser().isActive());
        assertEquals(SPECIALIZATION, actual.getSpecialization().getName());

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        Trainer updatedTrainer = captor.getValue();
        assertEquals(UPDATED_FIRST_NAME, updatedTrainer.getUser().getFirstName());
        assertEquals(UPDATED_LAST_NAME, updatedTrainer.getUser().getLastName());
        assertEquals(USERNAME, updatedTrainer.getUser().getUsername());
        assertTrue(updatedTrainer.getUser().isActive());
        assertEquals(SPECIALIZATION, updatedTrainer.getSpecialization().getName());

        verify(trainerRepository).findByUsername(USERNAME);
        verify(trainingTypeService).getByName(SPECIALIZATION);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_whenTrainerDoesNotExist_throwsServiceException() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.updateTrainer(UPDATE_REQUEST));

        assertEquals(TRAINER_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_whenTrainerExists_returnsTrainer() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINER));

        Trainer actual = service.getByUsername(USERNAME);

        assertTrainerFields(actual);
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_whenTrainerDoesNotExist_throwsServiceException() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.getByUsername(USERNAME));

        assertEquals(format("Trainer not found with username: %s", USERNAME), ex.getMessage());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void setTrainerActive_whenTrainerExists_updatesTrainerWithNewActiveStatus() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINER));

        service.setTrainerActivationStatus(USERNAME, false);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());

        Trainer updated = captor.getValue();
        assertFalse(updated.getUser().isActive());
        assertEquals(USERNAME, updated.getUser().getUsername());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void setTrainerActive_whenTrainerDoesNotExist_throwsServiceException() {
        when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.setTrainerActivationStatus(USERNAME, true));

        assertEquals(format("Trainer not found with username: %s", USERNAME), ex.getMessage());
        verify(trainerRepository).findByUsername(USERNAME);
    }

    @Test
    void getUnassignedForTrainee_returnsListOfUnassignedTrainers() {
        Trainee trainee = createTrainee();
        List<Trainer> trainerList = List.of(TRAINER);

        when(trainerRepository.findAllNotAssignedToTrainee(trainee)).thenReturn(trainerList);

        List<Trainer> actual = service.getUnassignedForTrainee(trainee);

        assertEquals(trainerList, actual);
        verify(trainerRepository).findAllNotAssignedToTrainee(trainee);
    }

    @Test
    void getTrainerTrainings_returnsListOfTrainings() {
        TrainerTrainingSearchCriteriaDto criteria = new TrainerTrainingSearchCriteriaDto();
        Training training = Training.builder().id(1L).build();
        List<Training> trainerTrainings = List.of(training);

        when(trainingQueryRepository.findTrainingsForTrainer(criteria)).thenReturn(trainerTrainings);

        List<Training> actual = service.getTrainerTrainings(criteria);

        assertEquals(trainerTrainings, actual);
        verify(trainingQueryRepository).findTrainingsForTrainer(criteria);
    }

    private static Trainer createTrainer() {
        return Trainer.builder()
                .user(createUser(USERNAME))
                .specialization(createTrainingType())
                .build();
    }

    private static User createUser(String username) {
        return User.builder()
                .username(username)
                .password(PASSWORD)
                .isActive(true)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of(new Role(1L, ROLE_TRAINER)))
                .build();
    }

    private static TrainingType createTrainingType() {
        return TrainingType.builder()
                .name(SPECIALIZATION)
                .build();
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .user(createUser("trainee.username"))
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Trainee Address")
                .build();
    }

    private static TrainerCreateRequestDto createTrainerCreateRequestDto() {
        TrainerCreateRequestDto dto = new TrainerCreateRequestDto();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setSpecialization(createTrainingType());

        return dto;
    }

    private static TrainerUpdateRequestDto createTrainerUpdateRequestDto() {
        TrainerUpdateRequestDto dto = new TrainerUpdateRequestDto();
        dto.setUsername(USERNAME);
        dto.setFirstName(UPDATED_FIRST_NAME);
        dto.setLastName(UPDATED_LAST_NAME);
        dto.setSpecialization(createTrainingType());
        dto.setIsActive(true);

        return dto;
    }

    private void assertTrainerFields(Trainer trainer) {
        assertEquals(FIRST_NAME, trainer.getUser().getFirstName());
        assertEquals(LAST_NAME, trainer.getUser().getLastName());
        assertEquals(USERNAME, trainer.getUser().getUsername());
        assertTrue(trainer.getUser().isActive());
        assertEquals(SPECIALIZATION, trainer.getSpecialization().getName());
    }
}