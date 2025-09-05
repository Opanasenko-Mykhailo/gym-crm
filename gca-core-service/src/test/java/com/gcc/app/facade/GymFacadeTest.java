package com.gcc.app.facade;

import com.gcc.app.facade.dto.AuthRequestDto;
import com.gcc.app.facade.dto.AuthResponseDto;
import com.gcc.app.facade.dto.LogoutRequestDto;
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
import com.gcc.app.mapper.TraineeMapper;
import com.gcc.app.mapper.TrainerMapper;
import com.gcc.app.mapper.TrainingMapper;
import com.gcc.app.mapper.TrainingTypeMapper;
import com.gcc.app.mapper.UserMapper;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import com.gcc.app.model.TrainingType;
import com.gcc.app.model.User;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final Long TRAINEE_ID = 1L;
    private static final String TRAINEE_FIRST_NAME = "John";
    private static final String TRAINEE_LAST_NAME = "Doe";
    private static final String TRAINEE_USERNAME = "john.doe";
    private static final LocalDate TRAINEE_DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    private static final String TRAINEE_ADDRESS = "123 Main St";

    private static final Long TRAINER_ID = 2L;
    private static final String TRAINER_FIRST_NAME = "Jane";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String SPECIALIZATION = "Yoga";

    private static final Long TRAINING_ID = 1L;
    private static final String TRAINING_NAME_YOGA = "Yoga";
    private static final String TRAINING_NAME_PILATES = "Pilates";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2025, 6, 30);
    private static final Long TRAINING_DURATION = 60L;

    private static final String USERNAME = "rowan.atkinson";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword";

    private final Trainee trainee = createTrainee();
    private final Trainer trainer = createTrainer();
    private final Training training = createTraining();
    private final TraineeCreateRequestDto traineeCreateRequestDto = createTraineeCreateRequestDto();
    private final TraineeUpdateRequestDto traineeUpdateRequestDto = createTraineeUpdateRequestDto();
    private final TrainingResponseDto expectedTrainingResponse = createTrainingResponseDto();
    private final TraineeCreateRequest traineeCreateRequest = createTraineeCreateRequest();
    private final TraineeUpdateRequest traineeUpdateRequest = createTraineeUpdateRequest();
    private final TrainerCreateRequest trainerCreateRequest = createTrainerCreateRequest();
    private final TrainerUpdateRequest trainerUpdateRequest = createTrainerUpdateRequest();
    private final TraineeUpdateResponse traineeUpdateResponse = createTraineeUpdateResponse();
    private final TraineeGetResponse traineeGetResponse = createTraineeGetResponse();
    private final TrainerUpdateResponse trainerUpdateResponse = createTrainerUpdateResponse();
    private final AuthRequestDto authRequestDto = createAuthRequestDto();
    private final LoginRequest loginRequest = createLoginRequest();
    private final PasswordChangeRequestDto passwordChangeRequestDto = createPasswordChangeRequestDto();
    private final ChangePasswordRequest changePasswordRequest = createChangePasswordRequest();

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TrainingTypeService trainingTypeService;
    @Mock
    private UserService userService;
    @Mock
    private AuthService authService;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;
    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private GymFacade facade;

    @Test
    void createTrainee_callsServiceAndReturnsUserCreationResponse() {
        when(traineeMapper.toCreateRequestDto(traineeCreateRequest)).thenReturn(traineeCreateRequestDto);
        when(traineeService.createTrainee(traineeCreateRequestDto)).thenReturn(trainee);

        UserCreationResponse actual = facade.createTrainee(traineeCreateRequest);

        assertEquals(TRAINEE_USERNAME, actual.getUsername());
        assertEquals(trainee.getUser().getPassword(), actual.getPassword());

        verify(traineeMapper).toCreateRequestDto(traineeCreateRequest);
        verify(traineeService).createTrainee(traineeCreateRequestDto);
    }

    @Test
    void updateTrainee_callsServiceAndReturnsUpdateResponse() {
        when(traineeMapper.toUpdateRequestDto(traineeUpdateRequest)).thenReturn(traineeUpdateRequestDto);
        when(traineeService.updateTrainee(traineeUpdateRequestDto)).thenReturn(trainee);
        when(traineeMapper.toUpdateRestModel(trainee)).thenReturn(traineeUpdateResponse);

        TraineeUpdateResponse actual = facade.updateTrainee(traineeUpdateRequest, TRAINEE_USERNAME);

        assertEquals(traineeUpdateResponse.getUsername(), actual.getUsername());
        assertEquals(traineeUpdateRequest.getFirstName(), actual.getFirstName());
        assertEquals(traineeUpdateResponse.getLastName(), actual.getLastName());

        verify(traineeMapper).toUpdateRequestDto(traineeUpdateRequest);
        verify(traineeService).updateTrainee(traineeUpdateRequestDto);
        verify(traineeMapper).toUpdateRestModel(trainee);
    }

    @Test
    void deleteTraineeByUsername_callsService() {
        facade.deleteTraineeByUsername(TRAINEE_USERNAME);
        verify(traineeService).deleteTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void getTraineeByUsername_callsServiceAndMapper_returnsTraineeGetResponse() {
        when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
        when(traineeMapper.toRestModel(trainee)).thenReturn(traineeGetResponse);

        TraineeGetResponse actual = facade.getTraineeByUsername(TRAINEE_USERNAME);

        assertEquals(traineeGetResponse.getUsername(), actual.getUsername());
        assertEquals(traineeGetResponse.getFirstName(), actual.getFirstName());
        assertEquals(traineeGetResponse.getLastName(), actual.getLastName());

        verify(traineeService).getByUsername(TRAINEE_USERNAME);
        verify(traineeMapper).toRestModel(trainee);
    }

    @Test
    void updateTraineeTrainers_callsServiceAndReturnsAssignedTrainersResponse() {
        List<String> trainerUsernames = List.of(TRAINER_USERNAME);
        TraineeAssignedTrainersUpdateResponse restResponse = new TraineeAssignedTrainersUpdateResponse();

        when(traineeService.updateTraineeTrainers(TRAINEE_USERNAME, trainerUsernames)).thenReturn(trainee);
        when(traineeMapper.toAssignedTrainersRestModel(trainee)).thenReturn(restResponse);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainers(TRAINEE_USERNAME, trainerUsernames);

        assertEquals(restResponse, actual);
        verify(traineeService).updateTraineeTrainers(TRAINEE_USERNAME, trainerUsernames);
        verify(traineeMapper).toAssignedTrainersRestModel(trainee);
    }

    @Test
    void setTraineeActive_callsServiceAndLogsAction() {
        facade.setTraineeActive(TRAINEE_USERNAME, true);

        verify(traineeService).setTraineeActivationStatus(TRAINEE_USERNAME, true);
        verifyNoMoreInteractions(traineeMapper);
    }

    @Test
    void setTrainerActive_callsServiceAndLogsAction() {
        facade.setTrainerActive(TRAINER_USERNAME, true);

        verify(trainerService).setTrainerActivationStatus(TRAINER_USERNAME, true);
        verifyNoMoreInteractions(trainerMapper);
    }

    @Test
    void getUnassignedTrainers_callsServiceAndMapper_returnsListOfTrainerResponseDto() {
        List<Trainer> trainers = List.of(trainer);
        AvailableTrainerGetResponse response = createAvailableTrainerGetResponse();

        when(traineeService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(trainers);
        when(trainerMapper.toAvailableTrainerRestModel(trainer)).thenReturn(response);

        List<AvailableTrainerGetResponse> actual = facade.getUnassignedTrainers(TRAINEE_USERNAME);

        assertEquals(1, actual.size());
        assertEquals(TRAINER_USERNAME, actual.get(0).getUsername());
        verify(traineeService).getUnassignedTrainers(TRAINEE_USERNAME);
        verify(trainerMapper).toAvailableTrainerRestModel(trainer);
    }

    @Test
    void createTrainer_callsServiceAndReturnsAuthResponse() {
        when(trainerMapper.toCreateRequestDto(trainerCreateRequest)).thenReturn(createTrainerCreateRequestDto());
        when(trainerService.createTrainer(createTrainerCreateRequestDto())).thenReturn(trainer);

        UserCreationResponse actual = facade.createTrainer(trainerCreateRequest);

        assertEquals(TRAINER_USERNAME, actual.getUsername());
        assertEquals(trainer.getUser().getPassword(), actual.getPassword());
        verify(trainerMapper).toCreateRequestDto(trainerCreateRequest);
        verify(trainerService).createTrainer(createTrainerCreateRequestDto());
    }

    @Test
    void updateTrainer_callsServiceAndMapper_returnsTrainerProfileResponse() {
        when(trainerMapper.toUpdateRequestDto(trainerUpdateRequest)).thenReturn(createTrainerUpdateRequestDto());
        when(trainerService.updateTrainer(createTrainerUpdateRequestDto())).thenReturn(trainer);
        when(trainerMapper.toUpdateRestModel(trainer)).thenReturn(trainerUpdateResponse);

        TrainerUpdateResponse actual = facade.updateTrainer(trainerUpdateRequest, TRAINER_USERNAME);

        assertEquals(trainerUpdateResponse.getUsername(), actual.getUsername());
        assertEquals(trainerUpdateResponse.getFirstName(), actual.getFirstName());
        assertEquals(trainerUpdateResponse.getLastName(), actual.getLastName());

        verify(trainerMapper).toUpdateRequestDto(trainerUpdateRequest);
        verify(trainerService).updateTrainer(createTrainerUpdateRequestDto());
        verify(trainerMapper).toUpdateRestModel(trainer);
    }

    @Test
    void getTrainerTrainings_callsServiceAndMapper_returnsListOfTrainingResponse() {
        TrainerTrainingSearchCriteriaDto criteria = new TrainerTrainingSearchCriteriaDto();
        List<Training> trainings = List.of(training);
        TrainerTrainingGetResponse response = createTrainerTrainingGetResponse();

        when(trainerService.getTrainerTrainings(criteria)).thenReturn(trainings);
        when(trainingMapper.toTrainerTrainingRestModel(training)).thenReturn(response);

        List<TrainerTrainingGetResponse> actual = facade.getTrainerTrainings(criteria);

        assertEquals(1, actual.size());
        assertEquals(response, actual.get(0));

        verify(trainerService).getTrainerTrainings(criteria);
        verify(trainingMapper).toTrainerTrainingRestModel(training);
    }

    @Test
    void createTraining_callsMapperAndService() {
        TrainingCreateRequest restRequest = new TrainingCreateRequest();
        restRequest.setTrainingName(TRAINING_NAME_YOGA);
        TrainingCreateRequestDto dto = createTrainingCreateRequestDto();

        when(trainingMapper.toTrainingCreateRequestDto(restRequest)).thenReturn(dto);

        facade.createTraining(restRequest);

        verify(trainingMapper).toTrainingCreateRequestDto(restRequest);
        verify(trainingService).createTraining(dto);
    }

    @Test
    void getTraining_callsServiceAndMapper_returnsTrainingResponseDto() {
        when(trainingService.getTraining(TRAINING_ID)).thenReturn(training);
        when(trainingMapper.toDto(training)).thenReturn(expectedTrainingResponse);

        TrainingResponseDto actual = facade.getTraining(TRAINING_ID);

        assertEquals(TRAINING_ID, actual.getId());
        assertEquals(TRAINEE_ID, actual.getTraineeId());
        assertEquals(TRAINER_ID, actual.getTrainerId());
        assertEquals(TRAINING_NAME_YOGA, actual.getName());
        assertEquals(SPECIALIZATION, actual.getType().getName());
        assertEquals(TRAINING_DATE, actual.getDate());
        assertEquals(TRAINING_DURATION, actual.getDuration());

        verify(trainingService).getTraining(TRAINING_ID);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getTraineeTrainings_callsServiceAndMapper_returnsListOfTraineeTrainingGetResponse() {
        TraineeTrainingSearchCriteriaDto criteria = new TraineeTrainingSearchCriteriaDto();
        List<Training> trainings = List.of(training);
        TraineeTrainingGetResponse response = createTraineeTrainingGetResponse();

        when(traineeService.getTraineeTrainings(criteria)).thenReturn(trainings);
        when(trainingMapper.toTraineeTrainingRestModel(training)).thenReturn(response);

        List<TraineeTrainingGetResponse> actual = facade.getTraineeTrainings(criteria);

        assertEquals(1, actual.size());
        assertEquals(TRAINING_NAME_YOGA, actual.get(0).getTrainingName());
        verify(traineeService).getTraineeTrainings(criteria);
        verify(trainingMapper).toTraineeTrainingRestModel(training);
    }

    @Test
    void getTrainerByUsername_callsServiceAndMapper_returnsTrainerProfileResponse() {
        TrainerGetResponse expected = new TrainerGetResponse();
        expected.setUsername(TRAINER_USERNAME);
        expected.setFirstName(TRAINER_FIRST_NAME);
        expected.setLastName(TRAINER_LAST_NAME);

        when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
        when(trainerMapper.toRestModel(trainer)).thenReturn(expected);

        TrainerGetResponse actual = facade.getTrainerByUsername(TRAINER_USERNAME);

        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());

        verify(trainerService).getByUsername(TRAINER_USERNAME);
        verify(trainerMapper).toRestModel(trainer);
    }

    @Test
    void getAllTrainingTypes_returnsMappedRestModels() {
        List<TrainingTypeResponseDto> dtoList = List.of(
                new TrainingTypeResponseDto(1L, TRAINING_NAME_YOGA),
                new TrainingTypeResponseDto(2L, TRAINING_NAME_PILATES)
        );

        List<TrainingTypeResponse> restList = List.of(
                new TrainingTypeResponse(TRAINING_NAME_YOGA, 1),
                new TrainingTypeResponse(TRAINING_NAME_PILATES, 2)
        );

        when(trainingTypeService.getAll()).thenReturn(dtoList);
        when(trainingTypeMapper.toRestModelList(dtoList)).thenReturn(restList);

        List<TrainingTypeResponse> actual = facade.getAllTrainingTypes();

        assertEquals(2, actual.size());
        assertEquals(TRAINING_NAME_YOGA, actual.get(0).getTrainingType());
        assertEquals(TRAINING_NAME_PILATES, actual.get(1).getTrainingType());

        verify(trainingTypeService).getAll();
        verify(trainingTypeMapper).toRestModelList(dtoList);
    }

    @Test
    void authenticate_callsService_returnsResponseDto() {
        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setSuccess(true);
        authResponseDto.setAccessToken("dummy.jwt.token");

        LoginResponse expectedLoginResponse = new LoginResponse();
        expectedLoginResponse.setSuccess(true);
        expectedLoginResponse.setAccessToken("dummy.jwt.token");

        when(userMapper.toAuthRequestDto(loginRequest)).thenReturn(authRequestDto);
        when(authService.authenticate(authRequestDto)).thenReturn(authResponseDto);
        when(userMapper.toLoginResponse(authResponseDto)).thenReturn(expectedLoginResponse);

        LoginResponse actual = facade.authenticate(loginRequest);

        assertTrue(actual.getSuccess(), "LoginResponse success should be true");
        assertEquals("dummy.jwt.token", actual.getAccessToken(), "Access token should match");
        verify(userMapper).toAuthRequestDto(loginRequest);
        verify(authService).authenticate(authRequestDto);
        verify(userMapper).toLoginResponse(authResponseDto);
    }

    @Test
    void changePassword_callsUserService() {
        when(userMapper.toPasswordChangeRequestDto(changePasswordRequest)).thenReturn(passwordChangeRequestDto);

        facade.changePassword(changePasswordRequest);

        verify(userMapper).toPasswordChangeRequestDto(changePasswordRequest);
        verify(userService).changePassword(passwordChangeRequestDto);
    }

    @Test
    void refreshToken_callsServiceAndMapper_returnsLoginResponse() {
        RefreshTokenRequest refreshTokenRequest = createRefreshTokenRequest();
        RefreshTokenRequestDto refreshTokenRequestDto = createRefreshTokenRequestDto();
        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setSuccess(true);
        authResponseDto.setAccessToken("new.dummy.jwt.token");

        LoginResponse expectedLoginResponse = new LoginResponse();
        expectedLoginResponse.setSuccess(true);
        expectedLoginResponse.setAccessToken("new.dummy.jwt.token");

        when(userMapper.toRefreshTokenRequestDto(refreshTokenRequest)).thenReturn(refreshTokenRequestDto);
        when(authService.refreshToken(refreshTokenRequestDto)).thenReturn(authResponseDto);
        when(userMapper.toLoginResponse(authResponseDto)).thenReturn(expectedLoginResponse);

        LoginResponse actual = facade.refreshToken(refreshTokenRequest);

        assertTrue(actual.getSuccess(), "LoginResponse success should be true");
        assertEquals("new.dummy.jwt.token", actual.getAccessToken(), "Access token should match");
        verify(userMapper).toRefreshTokenRequestDto(refreshTokenRequest);
        verify(authService).refreshToken(refreshTokenRequestDto);
        verify(userMapper).toLoginResponse(authResponseDto);
    }

    @Test
    void logout_callsServiceAndMapper() {
        RefreshTokenRequest refreshTokenRequest = createRefreshTokenRequest();
        LogoutRequestDto logoutRequestDto = createLogoutRequestDto();

        when(userMapper.toLogoutRequestDto(refreshTokenRequest)).thenReturn(logoutRequestDto);

        facade.logout(refreshTokenRequest);

        verify(userMapper).toLogoutRequestDto(refreshTokenRequest);
        verify(authService).logout(logoutRequestDto);
        verifyNoMoreInteractions(userMapper, authService);
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(createTraineeUser())
                .dateOfBirth(TRAINEE_DATE_OF_BIRTH)
                .address(TRAINEE_ADDRESS)
                .build();
    }

    private User createTraineeUser() {
        return User.builder()
                .firstName(TRAINEE_FIRST_NAME)
                .lastName(TRAINEE_LAST_NAME)
                .username(TRAINEE_USERNAME)
                .isActive(true)
                .build();
    }

    private Trainer createTrainer() {
        return Trainer.builder()
                .id(TRAINER_ID)
                .user(createTrainerUser())
                .specialization(createTrainingType())
                .build();
    }

    private User createTrainerUser() {
        return User.builder()
                .firstName(TRAINER_FIRST_NAME)
                .lastName(TRAINER_LAST_NAME)
                .username(TRAINER_USERNAME)
                .isActive(true)
                .build();
    }

    private Training createTraining() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainee(createTraineeForTraining())
                .trainer(createTrainerForTraining())
                .name(TRAINING_NAME_YOGA)
                .type(createTrainingType())
                .date(TRAINING_DATE)
                .duration(TRAINING_DURATION)
                .build();
    }

    private Trainee createTraineeForTraining() {
        return Trainee.builder().id(TRAINEE_ID).build();
    }

    private Trainer createTrainerForTraining() {
        return Trainer.builder().id(TRAINER_ID).build();
    }

    private TrainingType createTrainingType() {
        return TrainingType.builder().name(SPECIALIZATION).build();
    }

    private TraineeCreateRequestDto createTraineeCreateRequestDto() {
        TraineeCreateRequestDto dto = new TraineeCreateRequestDto();
        dto.setFirstName(TRAINEE_FIRST_NAME);
        dto.setLastName(TRAINEE_LAST_NAME);
        dto.setDateOfBirth(TRAINEE_DATE_OF_BIRTH);
        dto.setAddress(TRAINEE_ADDRESS);

        return dto;
    }

    private TraineeUpdateRequestDto createTraineeUpdateRequestDto() {
        TraineeUpdateRequestDto dto = new TraineeUpdateRequestDto();
        dto.setUsername(TRAINEE_USERNAME);
        dto.setFirstName(TRAINEE_FIRST_NAME);
        dto.setLastName(TRAINEE_LAST_NAME);
        dto.setDateOfBirth(TRAINEE_DATE_OF_BIRTH);
        dto.setAddress(TRAINEE_ADDRESS);
        dto.setIsActive(true);

        return dto;
    }

    private TrainerCreateRequestDto createTrainerCreateRequestDto() {
        TrainerCreateRequestDto dto = new TrainerCreateRequestDto();
        dto.setFirstName(TRAINER_FIRST_NAME);
        dto.setLastName(TRAINER_LAST_NAME);
        dto.setSpecialization(createTrainingType());

        return dto;
    }

    private TrainerUpdateRequestDto createTrainerUpdateRequestDto() {
        TrainerUpdateRequestDto dto = new TrainerUpdateRequestDto();
        dto.setUsername(TRAINER_USERNAME);
        dto.setFirstName(TRAINER_FIRST_NAME);
        dto.setLastName(TRAINER_LAST_NAME);
        dto.setSpecialization(createTrainingType());
        dto.setIsActive(true);

        return dto;
    }

    private TrainingCreateRequestDto createTrainingCreateRequestDto() {
        TrainingCreateRequestDto dto = new TrainingCreateRequestDto();
        dto.setTraineeUsername(TRAINEE_USERNAME);
        dto.setTrainerUsername(TRAINER_USERNAME);
        dto.setName(TRAINING_NAME_YOGA);
        dto.setType(createTrainingType());
        dto.setDate(TRAINING_DATE);
        dto.setDuration(TRAINING_DURATION);
        return dto;
    }

    private TrainingResponseDto createTrainingResponseDto() {
        TrainingResponseDto dto = new TrainingResponseDto();
        dto.setId(TRAINING_ID);
        dto.setTraineeId(TRAINEE_ID);
        dto.setTrainerId(TRAINER_ID);
        dto.setName(TRAINING_NAME_YOGA);
        dto.setType(createTrainingType());
        dto.setDate(TRAINING_DATE);
        dto.setDuration(TRAINING_DURATION);

        return dto;
    }

    private TrainerTrainingGetResponse createTrainerTrainingGetResponse() {
        TrainerTrainingGetResponse response = new TrainerTrainingGetResponse();
        response.setTraineeName(TRAINEE_USERNAME);
        response.setTrainingDate(TRAINING_DATE);
        response.setTrainingDuration(Math.toIntExact(TRAINING_DURATION));
        response.setTrainingName(TRAINING_NAME_YOGA);

        return response;
    }

    private TraineeTrainingGetResponse createTraineeTrainingGetResponse() {
        TraineeTrainingGetResponse response = new TraineeTrainingGetResponse();
        response.setTrainingName(TRAINING_NAME_YOGA);
        response.setTrainingDate(TRAINING_DATE);
        response.setTrainingDuration(Math.toIntExact(TRAINING_DURATION));
        response.setTrainerName(TRAINER_USERNAME);

        return response;
    }

    private AvailableTrainerGetResponse createAvailableTrainerGetResponse() {
        AvailableTrainerGetResponse response = new AvailableTrainerGetResponse();
        response.setUsername(TRAINER_USERNAME);
        response.setFirstName(TRAINER_FIRST_NAME);
        response.setLastName(TRAINER_LAST_NAME);
        response.setSpecialization(SPECIALIZATION);

        return response;
    }

    private TraineeCreateRequest createTraineeCreateRequest() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName(TRAINEE_FIRST_NAME);
        request.setLastName(TRAINEE_LAST_NAME);
        request.setDateOfBirth(TRAINEE_DATE_OF_BIRTH);
        request.setAddress(TRAINEE_ADDRESS);

        return request;
    }

    private TraineeUpdateRequest createTraineeUpdateRequest() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName(TRAINEE_FIRST_NAME);
        request.setLastName(TRAINEE_LAST_NAME);
        request.setDateOfBirth(TRAINEE_DATE_OF_BIRTH);
        request.setAddress(TRAINEE_ADDRESS);
        request.setIsActive(true);

        return request;
    }

    private TrainerCreateRequest createTrainerCreateRequest() {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName(TRAINER_FIRST_NAME);
        request.setLastName(TRAINER_LAST_NAME);

        return request;
    }

    private TrainerUpdateRequest createTrainerUpdateRequest() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName(TRAINER_FIRST_NAME);
        request.setLastName(TRAINER_LAST_NAME);

        return request;
    }

    private TraineeUpdateResponse createTraineeUpdateResponse() {
        TraineeUpdateResponse response = new TraineeUpdateResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setFirstName(TRAINEE_FIRST_NAME);
        response.setLastName(TRAINEE_LAST_NAME);

        return response;
    }

    private TraineeGetResponse createTraineeGetResponse() {
        TraineeGetResponse response = new TraineeGetResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setFirstName(TRAINEE_FIRST_NAME);
        response.setLastName(TRAINEE_LAST_NAME);

        return response;
    }

    private TrainerUpdateResponse createTrainerUpdateResponse() {
        TrainerUpdateResponse response = new TrainerUpdateResponse();
        response.setUsername(TRAINER_USERNAME);
        response.setFirstName(TRAINER_FIRST_NAME);
        response.setLastName(TRAINER_LAST_NAME);

        return response;
    }

    private AuthRequestDto createAuthRequestDto() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        return request;
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        return request;
    }

    private PasswordChangeRequestDto createPasswordChangeRequestDto() {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        return request;
    }

    private ChangePasswordRequest createChangePasswordRequest() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        return request;
    }

    private RefreshTokenRequest createRefreshTokenRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy.refresh.token");

        return request;
    }

    private RefreshTokenRequestDto createRefreshTokenRequestDto() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
        dto.setRefreshToken("dummy.refresh.token");

        return dto;
    }

    private LogoutRequestDto createLogoutRequestDto() {
        LogoutRequestDto dto = new LogoutRequestDto();
        dto.setRefreshToken("dummy.refresh.token");

        return dto;
    }
}