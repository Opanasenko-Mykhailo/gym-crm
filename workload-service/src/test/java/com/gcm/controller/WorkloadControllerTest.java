package com.gcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.gcm.app.rest.TrainerWorkloadRequest.ActionTypeEnum.ADD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkloadService service;

    @Captor
    private ArgumentCaptor<TrainerWorkloadRequest> workloadCaptor;

    @Test
    void givenValidRequest_whenPostWorkload_thenTrainerWorkloadProcessed() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setUsername("alice.smith");
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setActive(true);
        request.setDurationInMinutes(60);
        request.setActionType(ADD);
        request.setTrainingDate(LocalDate.now());

        mockMvc.perform(post("/api/workload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        then(service).should().processTrainerWorkload(workloadCaptor.capture(), org.mockito.ArgumentMatchers.anyString());
        assertThat(workloadCaptor.getValue())
                .extracting(TrainerWorkloadRequest::getUsername)
                .isEqualTo("alice.smith");
    }

    @Test
    void givenExistingTrainer_whenGetSummary_thenReturnsOk() throws Exception {
        TrainerSummaryRequest summary = new TrainerSummaryRequest();
        summary.setUsername("alice.smith");
        given(service.getTrainerSummary("alice.smith")).willReturn(summary);

        mockMvc.perform(get("/api/workload/alice.smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice.smith"));
    }

    @Test
    void givenNonExistingTrainer_whenGetSummary_thenReturnsNotFound() throws Exception {
        given(service.getTrainerSummary("unknown.user")).willReturn(null);

        mockMvc.perform(get("/api/workload/unknown.user"))
                .andExpect(status().isNotFound());
    }
}