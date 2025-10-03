package com.gcm.controller;

import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.exeption.ResourceNotFoundException;
import com.gcm.mapper.TrainerSummaryMapper;
import com.gcm.mapper.TrainerWorkloadMapper;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerSummaryResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService service;
    private final TrainerWorkloadMapper workloadMapper;
    private final TrainerSummaryMapper summaryMapper;

    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        service.processTrainerWorkload(workloadMapper.toDto(request));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryResponse> getTrainerSummary(@PathVariable String username) {
        TrainerSummaryResponseDto summary =service.getTrainerSummary(username);

        if (summary == null) {
            throw new ResourceNotFoundException(String.format("Trainer with username %s not found", username));
        }

        return ResponseEntity.ok(summaryMapper.toRestModel(summary));
    }
}