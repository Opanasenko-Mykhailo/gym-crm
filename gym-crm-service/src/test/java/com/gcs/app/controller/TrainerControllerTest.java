package com.gcs.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gcs.app.rest.ActivationStatusRequest;
import com.gcs.app.rest.TrainerCreateRequest;
import com.gcs.app.rest.TrainerGetResponse;
import com.gcs.app.rest.TrainerTrainingGetResponse;
import com.gcs.app.rest.TrainerUpdateRequest;
import com.gcs.app.rest.TrainerUpdateResponse;
import com.gcs.app.rest.UserCreationResponse;
import com.gcs.app.util.JsonReaderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest extends AbstractControllerTest {

    @Test
    void testRegisterTrainerSuccess() throws Exception {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setFirstName("Rowan");
        request.setLastName("Atkinson");
        request.specialization("Yoga");

        UserCreationResponse response = new UserCreationResponse();
        response.setUsername("rowan.atkinson");
        response.setPassword("secret");

        when(gymFacade.createTrainer(any())).thenReturn(response);

        var result = mockMvc.perform(post(basePath + "/trainers/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("rowan.atkinson"))
                .andExpect(jsonPath("$.password").value("secret"));
    }

    @Test
    void testGetTrainerProfileSuccess() throws Exception {
        TrainerGetResponse profile = new TrainerGetResponse();
        profile.setUsername("rowan.atkinson");
        profile.setFirstName("Rowan");
        profile.setLastName("Atkinson");

        when(gymFacade.getTrainerByUsername("rowan.atkinson")).thenReturn(profile);

        var result = mockMvc.perform(get(basePath + "/trainers/rowan.atkinson"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("rowan.atkinson"))
                .andExpect(jsonPath("$.firstName").value("Rowan"))
                .andExpect(jsonPath("$.lastName").value("Atkinson"));
    }

    @Test
    void testUpdateTrainerProfileSuccess() throws Exception {
        TrainerUpdateRequest request = JsonReaderUtil.readFromJson(
                "json/trainer-update-request.json",
                TrainerUpdateRequest.class
        );

        TrainerUpdateResponse response = new TrainerUpdateResponse();
        response.setUsername("rowan.atkinson");
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setIsActive(request.getIsActive());

        when(gymFacade.updateTrainer(any(), eq("rowan.atkinson"))).thenReturn(response);

        var result = mockMvc.perform(put(basePath + "/trainers/rowan.atkinson")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Trainer"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testChangeActivationStatusSuccess() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(false);

        var result = mockMvc.perform(patch(basePath + "/trainers/rowan.atkinson/change-activation-status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());
        verify(gymFacade).setTrainerActive("rowan.atkinson", false);
    }

    @Test
    void testGetTrainerTrainingsSuccess() throws Exception {
        List<TrainerTrainingGetResponse> trainings = JsonReaderUtil.readFromJson("json/get-trainer-trainings-response.json",
                new TypeReference<List<TrainerTrainingGetResponse>>() {
                });

        when(gymFacade.getTrainerTrainings(any())).thenReturn(trainings);

        var result = mockMvc.perform(get(basePath + "/trainers/rowan.atkinson/trainings")
                .param("periodFrom", "2025-07-01")
                .param("periodTo", "2025-07-31")
                .param("traineeName", "Jane"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Yoga"))
                .andExpect(jsonPath("$[0].trainingDate").value("2025-07-15"))
                .andExpect(jsonPath("$[0].trainingType").value("Stretching"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].traineeName").value("Jane"))
                .andExpect(jsonPath("$[1].trainingName").value("Pilates"))
                .andExpect(jsonPath("$[0].trainingDate").value("2025-07-15"))
                .andExpect(jsonPath("$[1].trainingType").value("Core"))
                .andExpect(jsonPath("$[1].trainingDuration").value(45))
                .andExpect(jsonPath("$[1].traineeName").value("Anna"));
    }

    @Test
    void testGetTrainerTrainingsInvalidDate() throws Exception {
        var result = mockMvc.perform(get(basePath + "/trainers/rowan.atkinson/trainings")
                .param("periodFrom", "invalid-date"));

        result.andExpect(status().isBadRequest());
    }
}