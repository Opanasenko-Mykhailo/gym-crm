package com.gcs.app.controller;

import com.gcs.app.facade.GymFacade;
import com.gcs.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcs.app.rest.ActivationStatusRequest;
import com.gcs.app.rest.TrainerCreateRequest;
import com.gcs.app.rest.TrainerGetResponse;
import com.gcs.app.rest.TrainerTrainingGetResponse;
import com.gcs.app.rest.TrainerUpdateRequest;
import com.gcs.app.rest.TrainerUpdateResponse;
import com.gcs.app.rest.UserCreationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final GymFacade gymFacade;

    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> registerTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        UserCreationResponse response = gymFacade.createTrainer(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('TRAINER')")
    public ResponseEntity<TrainerGetResponse> getTrainerProfile(@PathVariable String username) {
        TrainerGetResponse response = gymFacade.getTrainerByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasPermission(#username, 'TRAINER')")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(@PathVariable String username, @Valid @RequestBody TrainerUpdateRequest request) {
        TrainerUpdateResponse response = gymFacade.updateTrainer(request, username);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/change-activation-status")
    @PreAuthorize("hasPermission(#username, 'TRAINER')")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable String username, @Valid @RequestBody ActivationStatusRequest request) {
        gymFacade.setTrainerActive(username, request.getIsActive());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasAnyRole('TRAINER')")
    public ResponseEntity<List<TrainerTrainingGetResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {

        var criteria = new TrainerTrainingSearchCriteriaDto();
        criteria.setUsername(username);
        criteria.setFromDate(periodFrom);
        criteria.setToDate(periodTo);
        criteria.setTraineeName(traineeName);

        List<TrainerTrainingGetResponse> response = gymFacade.getTrainerTrainings(criteria);

        return ResponseEntity.ok(response);
    }
}