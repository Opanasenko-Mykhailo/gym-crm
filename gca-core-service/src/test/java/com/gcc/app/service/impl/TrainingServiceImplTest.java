package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TrainingCreateRequestDto;
import com.gcc.app.mapper.TrainingMapper;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import com.gcc.app.model.TrainingType;
import com.gcc.app.model.User;
import com.gcc.app.repository.TrainingRepository;
import com.gcc.app.service.TraineeService;
import com.gcc.app.service.TrainerService;
import com.gcc.app.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final String TRAINEE_USERNAME = "trainee.user";
    private static final String TRAINER_USERNAME = "trainer.user";
    private static final String NAME = "Yoga Session";
    private static final String TYPE = "Yoga";
    private static final LocalDate DATE = LocalDate.of(2025, 6, 30);
    private static final Long DURATION = 60L;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void createTraining_mapsDtoAndCreatesTraining_returnsTraining() {
        var requestDto = createTrainingCreateRequestDto();
        var trainee = createTrainee();
        var trainer = createTrainer();
        var type = createTrainingType();

        var mapped = Training.builder()
                .name(NAME)
                .date(DATE)
                .duration(DURATION)
                .build();
        var expected = mapped.toBuilder()
                .trainee(trainee)
                .trainer(trainer)
                .type(type)
                .build();

        when(trainingTypeService.getByName(TYPE)).thenReturn(type);
        when(trainingMapper.toEntity(requestDto)).thenReturn(mapped);
        when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
        when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
        when(trainingRepository.save(expected)).thenReturn(expected);

        Training actual = service.createTraining(requestDto);

        assertEquals(TRAINEE_USERNAME, actual.getTrainee().getUser().getUsername());
        assertEquals(TRAINER_USERNAME, actual.getTrainer().getUser().getUsername());
        assertEquals(NAME, actual.getName());
        assertEquals(TYPE, actual.getType().getName());
        assertEquals(DATE, actual.getDate());
        assertEquals(DURATION, actual.getDuration());

        verify(trainingTypeService).getByName(TYPE);
        verify(trainingMapper).toEntity(requestDto);
        verify(traineeService).getByUsername(TRAINEE_USERNAME);
        verify(trainerService).getByUsername(TRAINER_USERNAME);
        verify(trainingRepository).save(expected);
    }

    @Test
    void getTraining_whenTrainingExists_returnsTraining() {
        var expected = createTraining();

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(expected));

        Training actual = service.getTraining(1L);

        assertEquals(TRAINEE_USERNAME, actual.getTrainee().getUser().getUsername());
        assertEquals(TRAINER_USERNAME, actual.getTrainer().getUser().getUsername());
        assertEquals(NAME, actual.getName());
        assertEquals(TYPE, actual.getType().getName());
        assertEquals(DATE, actual.getDate());
        assertEquals(DURATION, actual.getDuration());

        verify(trainingRepository).findById(1L);
    }

    @Test
    void getTraining_whenTrainingDoesNotExist_throwsServiceException() {
        when(trainingRepository.findById(1L)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getTraining(1L));

        assertEquals("Training with id 1 not found", ex.getMessage());

        verify(trainingRepository).findById(1L);
    }

    private Training createTraining() {
        return Training.builder()
                .trainee(createTrainee())
                .trainer(createTrainer())
                .name(NAME)
                .type(createTrainingType())
                .date(DATE)
                .duration(DURATION)
                .build();
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .user(createUser(TRAINEE_USERNAME))
                .build();
    }

    private Trainer createTrainer() {
        return Trainer.builder()
                .user(createUser(TRAINER_USERNAME))
                .build();
    }

    private User createUser(String username) {
        return User.builder()
                .username(username)
                .build();
    }

    private TrainingType createTrainingType() {
        return TrainingType.builder()
                .name(TYPE)
                .build();
    }

    private TrainingCreateRequestDto createTrainingCreateRequestDto() {
        TrainingCreateRequestDto dto = new TrainingCreateRequestDto();
        dto.setTraineeUsername(TRAINEE_USERNAME);
        dto.setTrainerUsername(TRAINER_USERNAME);
        dto.setName(NAME);
        dto.setType(createTrainingType());
        dto.setDate(DATE);
        dto.setDuration(DURATION);

        return dto;
    }
}