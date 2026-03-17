package com.epam.gym.controller;

import com.epam.gym.dto.request.*;
import com.epam.gym.dto.response.*;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer Management", description = "Trainer profile and training management APIs")
public class TrainerController {

    private final GymFacade gymFacade;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @Operation(summary = "Register trainer", description = "Create a new trainer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {

        log.info("Registering trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecialization()
        );

        RegistrationResponse response = RegistrationResponse.builder()
                .username(trainer.getUser().getUsername())
                .password(trainer.getUser().getPassword())
                .build();

        log.info("Trainer registered successfully: {}", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password) {

        log.info("Fetching trainer profile: {}", username);

        gymFacade.authenticateTrainer(username, password);
        Trainer trainer = gymFacade.getTrainerByUsername(username);

        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        log.info("Trainer profile retrieved: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile", description = "Update trainer profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request) {

        log.info("Updating trainer profile: {}", username);

        // Validate username matches
        if (!username.equals(request.getUsername())) {
            throw new com.epam.gym.exception.ValidationException(
                    "Username in path does not match username in request body");
        }

        Trainer updatedData = Trainer.builder()
                .user(com.epam.gym.entity.User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isActive(request.getIsActive())
                        .build())
                .build();

        Trainer updated = gymFacade.updateTrainer(
                request.getUsername(),
                request.getPassword(),
                updatedData
        );

        TrainerProfileResponse response = trainerMapper.toProfileResponse(updated);

        log.info("Trainer profile updated: {}", username);
        return ResponseEntity.ok(response);
    }

    // {username}/trainings
    @Operation(summary = "Get trainer trainings",
            description = "Get trainer's training list with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainerTrainings(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password,
            @Parameter(description = "Filter by start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter by end date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainee name")
            @RequestParam(required = false) String traineeName) {

        log.info("Fetching trainings for trainer: {}", username);

        List<Training> trainings = gymFacade.getTrainerTrainingsByCriteria(
                username, password, fromDate, toDate, traineeName
        );

        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);

        log.info("Found {} trainings for trainer: {}", response.size(), username);
        return ResponseEntity.ok(response);
    }

    // {username}/status (PATCH for partial update)
    @Operation(summary = "Activate/Deactivate trainer",
            description = "Change trainer's active status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> toggleTrainerStatus(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Valid @RequestBody ToggleActiveRequest request) {

        log.info("Toggling active status for trainer: {}", username);

        // Validate username matches
        if (!username.equals(request.getUsername())) {
            throw new com.epam.gym.exception.ValidationException(
                    "Username in path does not match username in request body");
        }

        gymFacade.toggleTrainerStatus(username, request.getPassword());

        log.info("Active status toggled for trainer: {}", username);
        return ResponseEntity.ok().build();
    }
}