package com.gcc.app.service;

import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.facade.dto.TraineeCreateRequestDto;
import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TraineeUpdateRequestDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.User;
import com.gcc.app.repository.TraineeRepository;
import com.gcc.app.repository.UserRepository;
import com.gcc.app.security.BruteForceProtectionService;
import com.gcc.app.service.common.CredentialsService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ConstraintValidatorTest {
    private static final String USERNAME_REQUIRED = "Username is required";
    private static final String USERNAME_TOO_LONG = "Username must be at most 50 characters";
    private static final String OLD_PASSWORD_REQUIRED = "Old password is required";
    private static final String NEW_PASSWORD_REQUIRED = "New password is required";
    private static final String NEW_PASSWORD_TOO_SHORT = "New password must be at least 8 characters long";
    private static final String FIRST_NAME_REQUIRED = "First name is required";
    private static final String LAST_NAME_REQUIRED = "Last name is required";
    private static final String DATE_OF_BIRTH_REQUIRED = "Date of birth is required";
    private static final String DATE_OF_BIRTH_INVALID = "Date of birth must be in the past or today";
    private static final String ADDRESS_REQUIRED = "Address is required";
    private static final String ADDRESS_TOO_SHORT = "Address must be between 5 and 255 characters";
    private static final String FROM_DATE_INVALID = "From date must be in the past or present";
    private static final String TO_DATE_INVALID = "To date must be in the past or present";
    private static final String TRAINER_NAME_TOO_LONG = "Trainer name must be at most 100 characters";
    private static final String TRAINEE_NAME_TOO_LONG = "Trainee name must be at most 100 characters";
    private static final String TRAINING_TYPE_NAME_TOO_LONG = "Training type name must be at most 100 characters";

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TraineeRepository traineeRepository;

    @MockitoBean
    private CredentialsService credentialsService;

    @MockitoBean
    protected BruteForceProtectionService bruteForceService;

    @Nested
    @DisplayName("UserService dto validation test")
    class UserServiceTests {

        @Test
        void givenValidPasswordChangeDto_whenChangePassword_thenShouldNotThrow() {
            PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
            dto.setUsername("noah.taylor");
            dto.setOldPassword("OldPass1!");
            dto.setNewPassword("NewPass1!");

            User user = createUser();

            when(userRepository.findByUsername("noah.taylor")).thenReturn(Optional.of(user));
            when(credentialsService.isPasswordCorrect("OldPass1!", "ValidPass1!")).thenReturn(true);
            when(credentialsService.encodePassword("NewPass1!")).thenReturn("hashedNewPassword");
            when(userRepository.save(any())).thenReturn(user);

            assertDoesNotThrow(() -> userService.changePassword(dto));
        }

        @ParameterizedTest
        @MethodSource("com.gcc.app.service.ConstraintValidatorTest#provideInvalidPasswordChangeDtos")
        void givenInvalidPasswordChangeDto_whenChangePassword_thenShouldThrowConstraintViolationException(
                PasswordChangeRequestDto dto, String expectedMessage) {
            ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.changePassword(dto));
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Nested
    @DisplayName("TraineeService dto validation test")
    class TraineeServiceTests {

        @Test
        void givenValidTraineeCreateDto_whenCreateTrainee_thenShouldNotThrowException() {
            TraineeCreateRequestDto dto = new TraineeCreateRequestDto();
            dto.setFirstName("James");
            dto.setLastName("Wilson");
            dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
            dto.setAddress("123 Main St");

            Trainee savedTrainee = createTrainee();
            when(traineeRepository.save(any())).thenReturn(savedTrainee);

            assertDoesNotThrow(() -> traineeService.createTrainee(dto));
        }

        @ParameterizedTest
        @MethodSource("com.gcc.app.service.ConstraintValidatorTest#provideInvalidTraineeCreateDtos")
        void givenInvalidTraineeCreateDto_whenCreateTrainee_thenShouldThrowConstraintViolationException(
                TraineeCreateRequestDto dto, String expectedMessage) {
            ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> traineeService.createTrainee(dto));
            assertTrue(ex.getMessage().contains(expectedMessage));
        }

        @Test
        void givenValidTraineeTrainingDto_whenGetTraineeTrainings_thenShouldNotThrowException() {
            TraineeTrainingSearchCriteriaDto dto = new TraineeTrainingSearchCriteriaDto();
            dto.setUsername("sophia.martin");
            dto.setFromDate(LocalDate.now().minusDays(5));
            dto.setToDate(LocalDate.now());
            dto.setTrainerName("William Brown");
            dto.setTrainingTypeName("Type A");

            assertDoesNotThrow(() -> traineeService.getTraineeTrainings(dto));
        }

        @ParameterizedTest
        @MethodSource("com.gcc.app.service.ConstraintValidatorTest#provideInvalidTraineeTrainingDtos")
        void givenInvalidTraineeTrainingDto_whenGetTraineeTrainings_thenShouldThrowConstraintViolationException(
                TraineeTrainingSearchCriteriaDto dto, String expectedMessage) {
            ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> traineeService.getTraineeTrainings(dto));
            assertTrue(ex.getMessage().contains(expectedMessage));
        }

        @Test
        void givenValidTraineeUpdateDto_whenUpdateTrainee_thenValidationShouldPass() {
            TraineeUpdateRequestDto dto = new TraineeUpdateRequestDto();
            dto.setFirstName("Noah");
            dto.setLastName("Taylor");
            dto.setUsername("noah.taylor");
            dto.setIsActive(true);
            dto.setDateOfBirth(LocalDate.of(1985, 5, 15));
            dto.setAddress("456 Another St");

            Trainee existingTrainee = createTrainee();

            when(traineeRepository.findByUsername("noah.taylor")).thenReturn(Optional.of(existingTrainee));
            when(traineeRepository.save(any())).thenReturn(existingTrainee);

            assertDoesNotThrow(() -> traineeService.updateTrainee(dto));
        }

        @ParameterizedTest
        @MethodSource("com.gcc.app.service.ConstraintValidatorTest#provideInvalidTraineeUpdateDtos")
        void givenInvalidTraineeUpdateDto_whenUpdateTrainee_thenShouldThrowConstraintViolationException(
                TraineeUpdateRequestDto dto, String expectedMessage) {
            ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> traineeService.updateTrainee(dto));
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Nested
    @DisplayName("TrainerService dto validation test")
    class TrainerServiceTests {

        @Test
        void givenValidTrainerTrainingDto_whenGetTrainerTrainings_thenShouldNotThrowException() {
            TrainerService trainerService = mock(TrainerService.class);

            TrainerTrainingSearchCriteriaDto dto = new TrainerTrainingSearchCriteriaDto();
            dto.setUsername("liam.thompson");
            dto.setFromDate(LocalDate.now().minusDays(5));
            dto.setToDate(LocalDate.now());
            dto.setTraineeName("Olivia Davis");

            when(trainerService.getTrainerTrainings(any(TrainerTrainingSearchCriteriaDto.class)))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> trainerService.getTrainerTrainings(dto));
        }

        @ParameterizedTest
        @MethodSource("com.gcc.app.service.ConstraintValidatorTest#provideInvalidTrainerTrainingDtos")
        void givenInvalidTrainerTrainingDto_whenGetTrainerTrainings_thenShouldThrowConstraintViolationException(
                TrainerTrainingSearchCriteriaDto dto, String expectedMessage) {
            ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> trainerService.getTrainerTrainings(dto));
            assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    private static Stream<Arguments> provideInvalidPasswordChangeDtos() {
        return Stream.of(
                buildInvalidPasswordChangeDto(dto -> dto.setUsername(""), USERNAME_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setUsername(null), USERNAME_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setOldPassword(""), OLD_PASSWORD_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setOldPassword(null), OLD_PASSWORD_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setNewPassword(""), NEW_PASSWORD_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setNewPassword(null), NEW_PASSWORD_REQUIRED),
                buildInvalidPasswordChangeDto(dto -> dto.setNewPassword("short"), NEW_PASSWORD_TOO_SHORT)
        );
    }

    private static Stream<Arguments> provideInvalidTraineeCreateDtos() {
        return Stream.of(
                buildInvalidTraineeCreateDto(dto -> dto.setFirstName(""), FIRST_NAME_REQUIRED),
                buildInvalidTraineeCreateDto(dto -> dto.setFirstName(null), FIRST_NAME_REQUIRED),
                buildInvalidTraineeCreateDto(dto -> dto.setLastName(""), LAST_NAME_REQUIRED),
                buildInvalidTraineeCreateDto(dto -> dto.setLastName(null), LAST_NAME_REQUIRED),
                buildInvalidTraineeCreateDto(dto -> dto.setDateOfBirth(LocalDate.now().plusDays(1)), DATE_OF_BIRTH_INVALID),
                buildInvalidTraineeCreateDto(dto -> dto.setDateOfBirth(null), DATE_OF_BIRTH_REQUIRED),
                buildInvalidTraineeCreateDto(dto -> dto.setAddress("123"), ADDRESS_TOO_SHORT),
                buildInvalidTraineeCreateDto(dto -> dto.setAddress(null), ADDRESS_REQUIRED)
        );
    }

    private static Stream<Arguments> provideInvalidTrainerTrainingDtos() {
        return Stream.of(
                buildInvalidTrainerDto(dto -> dto.setUsername(""), USERNAME_REQUIRED),
                buildInvalidTrainerDto(dto -> dto.setUsername(null), USERNAME_REQUIRED),
                buildInvalidTrainerDto(dto -> dto.setUsername("a".repeat(51)), USERNAME_TOO_LONG),
                buildInvalidTrainerDto(dto -> dto.setFromDate(LocalDate.now().plusDays(1)), FROM_DATE_INVALID),
                buildInvalidTrainerDto(dto -> dto.setToDate(LocalDate.now().plusDays(2)), TO_DATE_INVALID),
                buildInvalidTrainerDto(dto -> dto.setTraineeName("a".repeat(101)), TRAINEE_NAME_TOO_LONG)
        );
    }

    private static Stream<Arguments> provideInvalidTraineeTrainingDtos() {
        return Stream.of(
                buildInvalidTraineeTrainingDto(dto -> dto.setUsername(""), USERNAME_REQUIRED),
                buildInvalidTraineeTrainingDto(dto -> dto.setUsername(null), USERNAME_REQUIRED),
                buildInvalidTraineeTrainingDto(dto -> dto.setTrainerName("a".repeat(101)), TRAINER_NAME_TOO_LONG),
                buildInvalidTraineeTrainingDto(dto -> dto.setTrainingTypeName("a".repeat(101)), TRAINING_TYPE_NAME_TOO_LONG)
        );
    }

    private static Stream<Arguments> provideInvalidTraineeUpdateDtos() {
        return Stream.of(
                buildInvalidDto(dto -> dto.setUsername(""), USERNAME_REQUIRED),
                buildInvalidDto(dto -> dto.setUsername(null), USERNAME_REQUIRED)
        );
    }

    private static Arguments buildInvalidDto(Consumer<TraineeUpdateRequestDto> modifier, String expectedMessage) {
        TraineeUpdateRequestDto dto = new TraineeUpdateRequestDto();
        dto.setFirstName("Noah");
        dto.setLastName("Taylor");
        dto.setUsername("noah.taylor");
        dto.setIsActive(true);
        dto.setDateOfBirth(LocalDate.of(1985, 5, 15));
        dto.setAddress("456 Another St");

        modifier.accept(dto);

        return Arguments.of(dto, expectedMessage);
    }

    private static Arguments buildInvalidTrainerDto(Consumer<TrainerTrainingSearchCriteriaDto> modifier, String expectedMessage) {
        TrainerTrainingSearchCriteriaDto dto = new TrainerTrainingSearchCriteriaDto();
        dto.setUsername("liam.thompson");
        dto.setFromDate(LocalDate.now().minusDays(1));
        dto.setToDate(LocalDate.now());
        dto.setTraineeName("Ava Clark");

        modifier.accept(dto);

        return Arguments.of(dto, expectedMessage);
    }

    private static Arguments buildInvalidTraineeCreateDto(Consumer<TraineeCreateRequestDto> modifier, String expectedMessage) {
        TraineeCreateRequestDto dto = new TraineeCreateRequestDto();
        dto.setFirstName("James");
        dto.setLastName("Wilson");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setAddress("123 Main St");

        modifier.accept(dto);

        return Arguments.of(dto, expectedMessage);
    }

    private static Arguments buildInvalidTraineeTrainingDto(Consumer<TraineeTrainingSearchCriteriaDto> modifier, String expectedMessage) {
        TraineeTrainingSearchCriteriaDto dto = new TraineeTrainingSearchCriteriaDto();
        dto.setUsername("sophia.martin");
        dto.setFromDate(LocalDate.now().minusDays(1));
        dto.setToDate(LocalDate.now());
        dto.setTrainerName("William Brown");
        dto.setTrainingTypeName("Yoga");

        modifier.accept(dto);

        return Arguments.of(dto, expectedMessage);
    }

    private static Arguments buildInvalidPasswordChangeDto(Consumer<PasswordChangeRequestDto> modifier, String expectedMessage) {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setUsername("emma.jackson");
        dto.setOldPassword("OldPass1!");
        dto.setNewPassword("NewPass1!");

        modifier.accept(dto);

        return Arguments.of(dto, expectedMessage);
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .user(createUser())
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("456 Another St")
                .build();
    }

    private User createUser() {
        return User.builder()
                .username("noah.taylor")
                .firstName("Noah")
                .lastName("Taylor")
                .password("ValidPass1!")
                .isActive(true)
                .build();
    }
}