package com.gcm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.mapper.TrainerSummaryMapper;
import com.gcm.mapper.TrainerWorkloadMapper;
import com.gcm.security.JwtService;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerSummaryResponseDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
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

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TrainerWorkloadMapper workloadMapper;

    @MockitoBean
    private TrainerSummaryMapper summaryMapper;

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

        given(workloadMapper.toDto(request)).willReturn(new TrainerWorkloadRequestDto());

        mockMvc.perform(post("/api/workload")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        then(service).should().processTrainerWorkload(workloadMapper.toDto(request));
    }

    @Test
    void givenExistingTrainer_whenGetSummary_thenReturnsOk() throws Exception {
        TrainerSummaryResponseDto dto = new TrainerSummaryResponseDto();
        dto.setUsername("alice.smith");

        TrainerSummaryResponse response = new TrainerSummaryResponse();
        response.setUsername("alice.smith");

        given(service.getTrainerSummary("alice.smith")).willReturn(dto);
        given(summaryMapper.toRestModel(dto)).willReturn(response);

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