package com.gcm.controller;

import com.gcm.model.TrainerSummary;
import com.gcm.model.TrainerWorkloadRequest;
import com.gcm.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class WorkloadController {

    private final WorkloadService service;

    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        String transactionId = UUID.randomUUID().toString();
        service.processTrainerWorkload(request, transactionId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummary> getTrainerSummary(@PathVariable String username) {
        TrainerSummary summary = service.getTrainerSummary(username);

        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }
}