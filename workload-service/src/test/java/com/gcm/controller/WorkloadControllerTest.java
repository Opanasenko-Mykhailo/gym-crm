package com.gcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.gcm.app.rest.TrainerWorkloadRequest.ActionTypeEnum.ADD;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Test
    void givenValidRequest_whenPostWorkload_thenReturnsOk() throws Exception {
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

        ArgumentCaptor<TrainerWorkloadRequest> captor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(service).processTrainerWorkload(captor.capture(), anyString());
        assert captor.getValue().getUsername().equals("alice.smith");
    }

    @Test
    void givenExistingTrainer_whenGetSummary_thenReturnsOk() throws Exception {
        TrainerSummaryRequest summary = new TrainerSummaryRequest();
        summary.setUsername("alice.smith");

        when(service.getTrainerSummary("alice.smith")).thenReturn(summary);

        mockMvc.perform(get("/api/workload/alice.smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice.smith"));
    }

    @Test
    void givenNonExistingTrainer_whenGetSummary_thenReturnsNotFound() throws Exception {
        when(service.getTrainerSummary("unknown.user")).thenReturn(null);

        mockMvc.perform(get("/api/workload/unknown.user"))
                .andExpect(status().isNotFound());
    }
}