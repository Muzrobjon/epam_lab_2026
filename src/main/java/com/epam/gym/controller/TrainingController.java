package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.response.ErrorResponse;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.repository.TrainingTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training Management", description = "Training creation and type management APIs")
public class TrainingController {

    private final GymFacade gymFacade;
    private final TrainingTypeRepository trainingTypeRepository;

    @Operation(summary = "Add training", description = "Create a new training session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        log.info("Adding training: {} for trainee: {} and trainer: {}",
                request.getTrainingName(),
                request.getTraineeUsername(),
                request.getTrainerUsername());

        // Get trainer to determine training type
        var trainer = gymFacade.getTrainerByUsername(request.getTrainerUsername());
        TrainingTypeName trainingType = trainer.getSpecialization().getTrainingTypeName();

        gymFacade.createTraining(
                request.getTraineeUsername(),
                request.getTraineePassword(),
                request.getTrainerUsername(),
                request.getTrainerPassword(),
                request.getTrainingName(),
                trainingType,
                request.getTrainingDate(),
                request.getTrainingDuration()
        );

        log.info("Training created successfully: {}", request.getTrainingName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Retrieve all available training types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainingTypeResponse.class)))
    })
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        log.info("Fetching all training types");

        List<TrainingType> types = trainingTypeRepository.findAll();
        List<TrainingTypeResponse> response = types.stream()
                .map(type -> TrainingTypeResponse.builder()
                        .id(type.getId())
                        .trainingTypeName(type.getTrainingTypeName())
                        .build())
                .collect(Collectors.toList());

        log.info("Found {} training types", response.size());
        return ResponseEntity.ok(response);
    }
}