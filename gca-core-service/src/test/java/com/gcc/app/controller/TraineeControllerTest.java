package com.gcc.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gcc.app.rest.ActivationStatusRequest;
import com.gcc.app.rest.AvailableTrainerGetResponse;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateRequest;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateResponse;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateResponseTrainersInner;
import com.gcc.app.rest.TraineeCreateRequest;
import com.gcc.app.rest.TraineeGetResponse;
import com.gcc.app.rest.TraineeTrainingGetResponse;
import com.gcc.app.rest.TraineeUpdateRequest;
import com.gcc.app.rest.TraineeUpdateResponse;
import com.gcc.app.rest.UserCreationResponse;
import com.gcc.app.util.JsonReaderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeControllerTest extends AbstractControllerTest {

    private static final String TRAINEE_USERNAME = "oleksandr.kovalenko";
    private static final String TRAINEE_FIRST_NAME = "Oleksandr";
    private static final String TRAINEE_LAST_NAME = "Kovalenko";
    private static final String TRAINEE_ADDRESS = "123 Main St";

    private static final String TRAINER_1_USERNAME = "sofia.melnyk";
    private static final String TRAINER_1_FIRST_NAME = "Sofia";
    private static final String TRAINER_1_LAST_NAME = "Melnyk";
    private static final String TRAINER_1_SPECIALIZATION = "Yoga";
    private static final String TRAINER_2_USERNAME = "andriy.shevchenko";
    private static final String TRAINER_2_FIRST_NAME = "Andriy";
    private static final String TRAINER_2_LAST_NAME = "Shevchenko";
    private static final String TRAINER_2_SPECIALIZATION = "Pilates";

    private static final String TRAINING_NAME_YOGA = "Yoga";
    private static final String TRAINING_NAME_PILATES = "Pilates";
    private static final int TRAINING_DURATION_YOGA = 60;
    private static final int TRAINING_DURATION_PILATES = 45;

    @Test
    void testRegisterTraineeSuccess() throws Exception {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName(TRAINEE_FIRST_NAME);
        request.setLastName(TRAINEE_LAST_NAME);
        request.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        request.setAddress(TRAINEE_ADDRESS);

        UserCreationResponse response = new UserCreationResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setPassword("secret");

        when(gymFacade.createTrainee(any())).thenReturn(response);

        var result = mockMvc.perform(post(basePath + "/trainees/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.password").value("secret"));

        verify(gymFacade).createTrainee(any(TraineeCreateRequest.class));
    }

    @Test
    void testGetTraineeProfileSuccess() throws Exception {
        TraineeGetResponse profile = new TraineeGetResponse();
        profile.setUsername(TRAINEE_USERNAME);
        profile.setFirstName(TRAINEE_FIRST_NAME);
        profile.setLastName(TRAINEE_LAST_NAME);

        when(gymFacade.getTraineeByUsername(TRAINEE_USERNAME)).thenReturn(profile);

        var result = mockMvc.perform(get(basePath + "/trainees/" + TRAINEE_USERNAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.firstName").value(TRAINEE_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(TRAINEE_LAST_NAME));

        verify(gymFacade).getTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void testUpdateTraineeProfileSuccess() throws Exception {
        TraineeUpdateRequest request = JsonReaderUtil.readFromJson(
                "json/trainee-update-request.json",
                TraineeUpdateRequest.class
        );

        TraineeUpdateResponse response = new TraineeUpdateResponse();
        response.setUsername(TRAINEE_USERNAME);
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setIsActive(request.getIsActive());

        when(gymFacade.updateTrainee(any(), eq(TRAINEE_USERNAME))).thenReturn(response);

        var result = mockMvc.perform(put(basePath + "/trainees/" + TRAINEE_USERNAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME))
                .andExpect(jsonPath("$.firstName").value(request.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(request.getLastName()))
                .andExpect(jsonPath("$.isActive").value(request.getIsActive()));

        verify(gymFacade).updateTrainee(any(TraineeUpdateRequest.class), eq(TRAINEE_USERNAME));
    }

    @Test
    void testDeleteTraineeProfileSuccess() throws Exception {
        var result = mockMvc.perform(delete(basePath + "/trainees/" + TRAINEE_USERNAME));

        result.andExpect(status().isOk());

        verify(gymFacade).deleteTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void testChangeActivationStatusSuccess() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(false);

        var result = mockMvc.perform(patch(basePath + "/trainees/" + TRAINEE_USERNAME + "/change-activation-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());

        verify(gymFacade).setTraineeActive(TRAINEE_USERNAME, false);
    }

    @Test
    void testGetAvailableTrainersSuccess() throws Exception {
        List<AvailableTrainerGetResponse> trainers = JsonReaderUtil.readFromJson(
                "json/available-trainers-response.json",
                new TypeReference<List<AvailableTrainerGetResponse>>() {
                }
        );

        when(gymFacade.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(trainers);

        var result = mockMvc.perform(get(basePath + "/trainees/" + TRAINEE_USERNAME + "/available-trainers"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(TRAINER_1_USERNAME))
                .andExpect(jsonPath("$[0].firstName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$[0].lastName").value(TRAINER_1_LAST_NAME))
                .andExpect(jsonPath("$[0].specialization").value(TRAINER_1_SPECIALIZATION))
                .andExpect(jsonPath("$[1].username").value(TRAINER_2_USERNAME))
                .andExpect(jsonPath("$[1].firstName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$[1].lastName").value(TRAINER_2_LAST_NAME))
                .andExpect(jsonPath("$[1].specialization").value(TRAINER_2_SPECIALIZATION));

        verify(gymFacade).getUnassignedTrainers(TRAINEE_USERNAME);
    }

    @Test
    void testUpdateTraineeTrainersSuccess() throws Exception {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest();
        request.setTrainerUsernames(List.of(TRAINER_1_USERNAME, TRAINER_2_USERNAME));

        TraineeAssignedTrainersUpdateResponse response = new TraineeAssignedTrainersUpdateResponse();
        TraineeAssignedTrainersUpdateResponseTrainersInner trainer1 = new TraineeAssignedTrainersUpdateResponseTrainersInner();
        trainer1.setUsername(TRAINER_1_USERNAME);
        trainer1.setFirstName(TRAINER_1_FIRST_NAME);
        trainer1.setLastName(TRAINER_1_LAST_NAME);
        trainer1.setSpecialization(TRAINER_1_SPECIALIZATION);
        TraineeAssignedTrainersUpdateResponseTrainersInner trainer2 = new TraineeAssignedTrainersUpdateResponseTrainersInner();
        trainer2.setUsername(TRAINER_2_USERNAME);
        trainer2.setFirstName(TRAINER_2_FIRST_NAME);
        trainer2.setLastName(TRAINER_2_LAST_NAME);
        trainer2.setSpecialization(TRAINER_2_SPECIALIZATION);
        response.setTrainers(List.of(trainer1, trainer2));

        when(gymFacade.updateTraineeTrainers(eq(TRAINEE_USERNAME), any())).thenReturn(response);

        var result = mockMvc.perform(put(basePath + "/trainees/" + TRAINEE_USERNAME + "/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers[0].username").value(TRAINER_1_USERNAME))
                .andExpect(jsonPath("$.trainers[0].firstName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$.trainers[0].lastName").value(TRAINER_1_LAST_NAME))
                .andExpect(jsonPath("$.trainers[0].specialization").value(TRAINER_1_SPECIALIZATION))
                .andExpect(jsonPath("$.trainers[1].username").value(TRAINER_2_USERNAME))
                .andExpect(jsonPath("$.trainers[1].firstName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$.trainers[1].lastName").value(TRAINER_2_LAST_NAME))
                .andExpect(jsonPath("$.trainers[1].specialization").value(TRAINER_2_SPECIALIZATION));

        verify(gymFacade).updateTraineeTrainers(eq(TRAINEE_USERNAME), any());
    }

    @Test
    void testGetTraineeTrainingsSuccess() throws Exception {
        List<TraineeTrainingGetResponse> trainings = JsonReaderUtil.readFromJson(
                "json/get-trainee-trainings-response.json",
                new TypeReference<List<TraineeTrainingGetResponse>>() {
                }
        );

        when(gymFacade.getTraineeTrainings(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(basePath + "/trainees/" + TRAINEE_USERNAME + "/trainings")
                .param("periodFrom", "2025-07-01")
                .param("periodTo", "2025-07-31")
                .param("trainerName", TRAINER_1_FIRST_NAME));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value(TRAINING_NAME_YOGA))
                .andExpect(jsonPath("$[0].trainingDate").value("2025-07-15"))
                .andExpect(jsonPath("$[0].trainerName").value(TRAINER_1_FIRST_NAME))
                .andExpect(jsonPath("$[0].trainingDuration").value(TRAINING_DURATION_YOGA))
                .andExpect(jsonPath("$[1].trainingName").value(TRAINING_NAME_PILATES))
                .andExpect(jsonPath("$[1].trainingDate").value("2025-07-20"))
                .andExpect(jsonPath("$[1].trainerName").value(TRAINER_2_FIRST_NAME))
                .andExpect(jsonPath("$[1].trainingDuration").value(TRAINING_DURATION_PILATES));

        verify(gymFacade).getTraineeTrainings(any());
    }

    @Test
    void testGetTraineeTrainingsInvalidDate() throws Exception {
        var result = mockMvc.perform(get(basePath + "/trainees/" + TRAINEE_USERNAME + "/trainings")
                .param("periodFrom", "invalid-date"));

        result.andExpect(status().isBadRequest());
    }
}