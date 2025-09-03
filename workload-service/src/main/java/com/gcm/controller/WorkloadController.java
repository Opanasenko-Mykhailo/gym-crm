package com.gcm.controller;

import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.exeption.ResourceNotFoundException;
import com.gcm.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService service;

    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        service.processTrainerWorkload(request, UUID.randomUUID().toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryRequest> getTrainerSummary(@PathVariable String username) {
        TrainerSummaryRequest summary = service.getTrainerSummary(username);

        if (summary == null) {
            throw new ResourceNotFoundException("Trainer with username " + username + " not found");
        }

        return ResponseEntity.ok(summary);
    }
}