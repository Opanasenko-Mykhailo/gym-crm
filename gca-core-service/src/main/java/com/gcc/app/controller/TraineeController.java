package com.gcc.app.controller;

import com.gcc.app.facade.GymFacade;
import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.rest.ActivationStatusRequest;
import com.gcc.app.rest.AvailableTrainerGetResponse;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateRequest;
import com.gcc.app.rest.TraineeAssignedTrainersUpdateResponse;
import com.gcc.app.rest.TraineeCreateRequest;
import com.gcc.app.rest.TraineeGetResponse;
import com.gcc.app.rest.TraineeTrainingGetResponse;
import com.gcc.app.rest.TraineeUpdateRequest;
import com.gcc.app.rest.TraineeUpdateResponse;
import com.gcc.app.rest.UserCreationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("${app.api.base-path}/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final GymFacade gymFacade;

    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> registerTrainee(@Valid @RequestBody TraineeCreateRequest request) {
        UserCreationResponse response = gymFacade.createTrainee(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('TRAINER', 'TRAINEE')")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable String username) {
        TraineeGetResponse response = gymFacade.getTraineeByUsername(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasPermission(#username, 'TRAINEE')")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable String username, @Valid @RequestBody TraineeUpdateRequest request) {
        TraineeUpdateResponse response = gymFacade.updateTrainee(request, username);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasPermission(#username, 'TRAINEE')")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        gymFacade.deleteTraineeByUsername(username);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}/change-activation-status")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Void> changeActivationStatus(@PathVariable String username, @Valid @RequestBody ActivationStatusRequest request) {
        gymFacade.setTraineeActive(username, request.getIsActive());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/available-trainers")
    @PreAuthorize("hasPermission(#username, 'TRAINEE')")
    public ResponseEntity<List<AvailableTrainerGetResponse>> getAvailableTrainers(@PathVariable String username) {
        List<AvailableTrainerGetResponse> response = gymFacade.getUnassignedTrainers(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    @PreAuthorize("hasPermission(#username, 'TRAINEE')")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable String username, @Valid @RequestBody TraineeAssignedTrainersUpdateRequest request) {
        TraineeAssignedTrainersUpdateResponse response = gymFacade.updateTraineeTrainers(username, request.getTrainerUsernames());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasAnyRole('TRAINER', 'TRAINEE')")
    public ResponseEntity<List<TraineeTrainingGetResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName) {

        var criteria = new TraineeTrainingSearchCriteriaDto();
        criteria.setUsername(username);
        criteria.setFromDate(periodFrom);
        criteria.setToDate(periodTo);
        criteria.setTrainerName(trainerName);

        List<TraineeTrainingGetResponse> response = gymFacade.getTraineeTrainings(criteria);

        return ResponseEntity.ok(response);
    }
}