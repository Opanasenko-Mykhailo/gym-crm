package com.gcs.app.controller;

import com.gcs.app.facade.GymFacade;
import com.gcs.app.rest.ErrorResponse;
import com.gcs.app.rest.TrainingTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/training-types")
@RequiredArgsConstructor
@Tag(name = "Training", description = "Operations related to training sessions")
public class TrainingTypeController {

    private final GymFacade facade;

    @Operation(summary = "Get training types", description = "Retrieves the list of available training types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of training types",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainingTypeResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('TRAINER', 'TRAINEE')")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> trainingTypes = facade.getAllTrainingTypes();

        return ResponseEntity.ok(trainingTypes);
    }
}