package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TraineeCreateRequestDto;
import com.gcc.app.facade.dto.TraineeUpdateRequestDto;
import com.gcc.app.mapper.TraineeMapper;
import com.gcc.app.model.Role;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.User;
import com.gcc.app.repository.TraineeRepository;
import com.gcc.app.service.RoleService;
import com.gcc.app.service.TrainerService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.gcc.app.model.enums.RoleType.ROLE_TRAINEE;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final String USERNAME = "soren.halvorsen";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "Soren";
    private static final String LAST_NAME = "Halvorsen";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final String TRAINEE_NOT_FOUND_MESSAGE = format("Trainee not found with username: %s", USERNAME);

    private static final Trainee TRAINEE = createTrainee();
    private static final TraineeCreateRequestDto CREATE_REQUEST = createTraineeCreateRequestDto();
    private static final TraineeUpdateRequestDto UPDATE_REQUEST = createTraineeUpdateRequestDto();

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private UserService userService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void createTrainee_mapsDtoAndCreatesTrainee_returnsTrainee() {
        when(roleService.getByType(ROLE_TRAINEE)).thenReturn(new Role(1L, ROLE_TRAINEE));
        when(traineeMapper.toEntity(CREATE_REQUEST)).thenReturn(TRAINEE);
        when(userService.getAllUsernames()).thenReturn(Collections.emptySet());
        when(credentialsService.generateUsername(any(), any(), any())).thenReturn(USERNAME);
        when(credentialsService.generateRandomPassword()).thenReturn(PASSWORD);
        when(credentialsService.encodePassword(PASSWORD)).thenReturn("encodedPass");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(TRAINEE);

        Trainee actual = service.createTrainee(CREATE_REQUEST);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().isActive());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());

        verify(traineeMapper).toEntity(CREATE_REQUEST);
        verify(userService).getAllUsernames();
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_whenTraineeExists_mapsDtoUpdatesAndReturnsTrainee() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee actual = service.updateTrainee(UPDATE_REQUEST);

        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertEquals(USERNAME, actual.getUser().getUsername());
        assertTrue(actual.getUser().isActive());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        Trainee updated = captor.getValue();

        assertEquals(FIRST_NAME, updated.getUser().getFirstName());
        assertEquals(LAST_NAME, updated.getUser().getLastName());
        assertEquals(USERNAME, updated.getUser().getUsername());
        assertTrue(updated.getUser().isActive());
        assertEquals(DATE_OF_BIRTH, updated.getDateOfBirth());
        assertEquals(ADDRESS, updated.getAddress());

        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void updateTrainee_whenTraineeDoesNotExist_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateTrainee(UPDATE_REQUEST));

        assertEquals(TRAINEE_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void deleteTraineeByUsername_whenTraineeExists_deletesTrainee() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));

        service.deleteTraineeByUsername(USERNAME);

        verify(traineeRepository).findByUsername(USERNAME);
        verify(traineeRepository).deleteByUser_Username(USERNAME);
    }

    @Test
    void deleteTraineeByUsername_whenTraineeDoesNotExist_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteTraineeByUsername(USERNAME));

        assertEquals(TRAINEE_NOT_FOUND_MESSAGE, ex.getMessage());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_whenTraineeExists_returnsTrainee() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));

        Trainee actual = service.getByUsername(USERNAME);

        assertEquals(USERNAME, actual.getUser().getUsername());
        assertEquals(FIRST_NAME, actual.getUser().getFirstName());
        assertEquals(LAST_NAME, actual.getUser().getLastName());
        assertTrue(actual.getUser().isActive());
        assertEquals(DATE_OF_BIRTH, actual.getDateOfBirth());
        assertEquals(ADDRESS, actual.getAddress());

        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void getByUsername_whenTraineeDoesNotExist_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getByUsername(USERNAME));

        assertEquals(format("Trainee not found with username: %s", USERNAME), ex.getMessage());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void setTraineeActive_whenTraineeExists_updatesIsActiveAndReturnsTrainee() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));

        service.setTraineeActivationStatus(USERNAME, false);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());

        Trainee updated = captor.getValue();
        assertFalse(updated.getUser().isActive());
        assertEquals(USERNAME, updated.getUser().getUsername());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void setTraineeActive_whenTraineeNotFound_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.setTraineeActivationStatus(USERNAME, true));

        assertEquals(format("Trainee not found with username: %s", USERNAME), ex.getMessage());
        verify(traineeRepository).findByUsername(USERNAME);
    }

    @Test
    void getUnassignedTrainers_whenTraineeExists_returnsTrainerList() {
        List<Trainer> expected = List.of(createTrainer("trainer.mock"));

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));
        when(trainerService.getUnassignedForTrainee(TRAINEE)).thenReturn(expected);

        List<Trainer> result = service.getUnassignedTrainers(USERNAME);

        assertEquals(expected, result);
        verify(trainerService).getUnassignedForTrainee(TRAINEE);
    }

    @Test
    void getUnassignedTrainers_whenTraineeNotFound_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.getUnassignedTrainers(USERNAME));

        assertEquals(format("Trainee not found with username: %s", USERNAME), ex.getMessage());
    }

    @Test
    void updateTraineeTrainers_whenTraineeExists_assignsNewTrainers() {
        Trainer trainer1 = createTrainer("trainer.one").toBuilder().id(1L).build();
        Trainer trainer2 = createTrainer("trainer.two").toBuilder().id(2L).build();
        Trainer oldTrainer = createTrainer("old.trainer").toBuilder().id(3L).build();

        oldTrainer.getTrainees().add(TRAINEE);
        TRAINEE.getTrainers().add(oldTrainer);
        List<String> trainerUsernames = List.of("trainer.one", "trainer.two");

        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(TRAINEE));
        when(trainerService.getByUsername("trainer.one")).thenReturn(trainer1);
        when(trainerService.getByUsername("trainer.two")).thenReturn(trainer2);
        when(traineeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee actual = service.updateTraineeTrainers(USERNAME, trainerUsernames);

        Set<Trainer> updatedTrainers = actual.getTrainers();
        assertEquals(2, updatedTrainers.size());
        assertFalse(updatedTrainers.contains(oldTrainer));
        assertTrue(updatedTrainers.contains(trainer1));
        assertTrue(updatedTrainers.contains(trainer2));

        verify(traineeRepository).findByUsername(USERNAME);
        verify(trainerService).getByUsername("trainer.one");
        verify(trainerService).getByUsername("trainer.two");
        verify(traineeRepository).save(any());
    }

    @Test
    void updateTraineeTrainers_whenTraineeNotFound_throwsServiceException() {
        when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateTraineeTrainers(USERNAME, List.of("trainer1")));

        assertEquals(format("Trainee not found with username: %s", USERNAME), ex.getMessage());
    }

    private static Trainee createTrainee() {
        return Trainee.builder()
                .user(createUser())
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new HashSet<>())
                .build();
    }

    private static User createUser() {
        return User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .roles(Set.of(new Role(1L, ROLE_TRAINEE)))
                .build();
    }

    private static TraineeCreateRequestDto createTraineeCreateRequestDto() {
        TraineeCreateRequestDto dto = new TraineeCreateRequestDto();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setDateOfBirth(DATE_OF_BIRTH);
        dto.setAddress(ADDRESS);

        return dto;
    }

    private static TraineeUpdateRequestDto createTraineeUpdateRequestDto() {
        TraineeUpdateRequestDto dto = new TraineeUpdateRequestDto();
        dto.setUsername(USERNAME);
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setDateOfBirth(DATE_OF_BIRTH);
        dto.setAddress(ADDRESS);
        dto.setIsActive(true);

        return dto;
    }

    private Trainer createTrainer(String username) {
        User user = User.builder()
                .username(username)
                .firstName("Trainer")
                .lastName("Test")
                .isActive(true)
                .build();

        return Trainer.builder()
                .user(user)
                .trainees(new HashSet<>())
                .build();
    }
}