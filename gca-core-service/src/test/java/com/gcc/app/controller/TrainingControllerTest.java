package com.gcc.app.controller;

import com.gcc.app.rest.TrainingCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingControllerTest extends AbstractControllerTest {

    private static final String TRAINEE_USERNAME = "oleksandr.kovalenko";
    private static final String TRAINER_USERNAME = "sofia.melnyk";
    private static final String TRAINING_NAME_YOGA = "Yoga";
    private static final String TRAINING_TYPE_NAME_YOGA = "YOGA";
    private static final LocalDate TRAINING_DATE = LocalDate.of(1975, 6, 20);
    private static final int TRAINING_DURATION = 60;

    private final TrainingCreateRequest trainingCreateRequest = createTrainingCreateRequest();

    @Test
    void testAddTrainingSuccess() throws Exception {
        mockMvc.perform(post(basePath + "/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainingCreateRequest)))
                .andExpect(status().isOk());

        verify(gymFacade).createTraining(any(TrainingCreateRequest.class));
    }

    private TrainingCreateRequest createTrainingCreateRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTrainingName(TRAINING_NAME_YOGA);
        request.setTrainingTypeName(TRAINING_TYPE_NAME_YOGA);
        request.setTraineeUsername(TRAINEE_USERNAME);
        request.setTrainerUsername(TRAINER_USERNAME);
        request.setTrainingDate(TRAINING_DATE);
        request.setTrainingDuration(TRAINING_DURATION);

        return request;
    }
}