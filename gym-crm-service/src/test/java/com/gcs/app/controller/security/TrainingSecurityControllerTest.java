package com.gcs.app.controller.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.gcs.app.util.JsonReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TrainingSecurityControllerTest extends AbstractSecurityControllerTest {

    private static final String TEST_DATA_PATH = "json/security/training-test-data.json";

    private static JsonNode testData;

    @BeforeAll
    static void setUp() {
        testData = JsonReaderUtil.readFromJson(TEST_DATA_PATH, JsonNode.class);
    }

    @Test
    @WithAnonymousUser
    void createTraining_shouldBeUnauthorizedWithoutAuth() throws Exception {
        mockMvc.perform(post(basePath + "/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testData.get("trainingCreateRequest").toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "trainer.user", roles = {"TRAINER"})
    void createTraining_shouldBeAccessibleForTrainer() throws Exception {
        mockMvc.perform(post(basePath + "/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testData.get("trainingCreateRequest").toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "trainee.user", roles = {"TRAINEE"})
    void createTraining_shouldBeForbiddenForTrainee() throws Exception {
        mockMvc.perform(post(basePath + "/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testData.get("trainingCreateRequest").toString()))
                .andExpect(status().isForbidden());
    }
}