package com.epam.gym.controller;

import com.epam.gym.dto.request.*;
import com.epam.gym.dto.response.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.mapper.TraineeMapper;
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
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management", description = "Trainee profile and training management APIs")
public class TraineeController {

    private final GymFacade gymFacade;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;

    @Operation(summary = "Register trainee", description = "Create a new trainee profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee created successfully",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request) {

        log.info("Registering trainee: {} {}", request.getFirstName(), request.getLastName());

        Trainee trainee = gymFacade.createTrainee(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress()
        );

        // TODO:
        //  [Optional]
        //  You have mapstruct, let's use it here as well. And since registration response consists of only profile
        //  related fields for both Trainer and Trainee, I would say introducing UserMapper would be a good idea
        RegistrationResponse response = RegistrationResponse.builder()
                .username(trainee.getUser().getUsername())
                .password(trainee.getUser().getPassword())
                .build();

        log.info("Trainee registered successfully: {}", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password) {

        log.info("Fetching trainee profile: {}", username);

        gymFacade.authenticateTrainee(username, password);
        Trainee trainee = gymFacade.getTraineeByUsername(username);

        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        log.info("Trainee profile retrieved: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile", description = "Update trainee profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {

        log.info("Updating trainee profile: {}", username);

        // Validate username matches
        if (!username.equals(request.getUsername())) {
            throw new com.epam.gym.exception.ValidationException(
                    "Username in path does not match username in request body");
        }

        // TODO:
        //  Why to introduce additional instances? You can instead pass the DTO to the service layer
        //  Ideally, the only moment when you instantiate an entity is when you create a new one.
        Trainee updatedData = Trainee.builder()
                .user(com.epam.gym.entity.User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isActive(request.getIsActive())
                        .build())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        Trainee updated = gymFacade.updateTrainee(
                request.getUsername(),
                request.getPassword(),
                updatedData
        );

        TraineeProfileResponse response = traineeMapper.toProfileResponse(updated);

        log.info("Trainee profile updated: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Delete trainee profile and associated trainings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            // TODO:
            //  Danger!!! password in query parameters is insecure
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password) {

        log.info("Deleting trainee profile: {}", username);

        gymFacade.deleteTrainee(username, password);

        log.info("Trainee profile deleted: {}", username);
        return ResponseEntity.noContent().build();
    }

    // {username}/trainers/unassigned
    @Operation(summary = "Get unassigned trainers",
            description = "Get list of active trainers not assigned to the trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainerSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainers/unassigned")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password) {

        log.info("Fetching unassigned trainers for trainee: {}", username);

        gymFacade.authenticateTrainee(username, password);
        List<Trainer> trainers = gymFacade.getUnassignedTrainers(username);
        List<TrainerSummaryResponse> response = trainerMapper.toSummaryResponseList(trainers);

        log.info("Found {} unassigned trainers for trainee: {}", response.size(), username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee's trainers list",
            description = "Update the list of trainers assigned to a trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers list updated successfully",
                    content = @Content(schema = @Schema(implementation = TrainerSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTraineeTrainersList(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        log.info("Updating trainers list for trainee: {}", username);

        // Validate username matches
        if (!username.equals(request.getTraineeUsername())) {
            throw new com.epam.gym.exception.ValidationException(
                    "Username in path does not match username in request body");
        }

        gymFacade.updateTraineeTrainersList(
                request.getTraineeUsername(),
                request.getPassword(),
                request.getTrainerUsernames()
        );

        Trainee trainee = gymFacade.getTraineeByUsername(request.getTraineeUsername());
        List<TrainerSummaryResponse> response = trainerMapper.toSummaryResponseList(trainee.getTrainers());

        log.info("Trainers list updated for trainee: {}", username);
        return ResponseEntity.ok(response);
    }

    // ✅ MORE RESTFUL: /{username}/trainings
    @Operation(summary = "Get trainee trainings",
            description = "Get trainee's training list with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrainingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestParam String password,
            @Parameter(description = "Filter by start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter by end date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainer name")
            @RequestParam(required = false) String trainerName,
            @Parameter(description = "Filter by training type")
            @RequestParam(required = false) TrainingTypeName trainingType) {

        log.info("Fetching trainings for trainee: {}", username);

        List<Training> trainings = gymFacade.getTraineeTrainingsByCriteria(
                username, password, fromDate, toDate, trainerName, trainingType
        );

        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);

        log.info("Found {} trainings for trainee: {}", response.size(), username);
        return ResponseEntity.ok(response);
    }

    // {username}/status (PATCH for partial update)
    @Operation(summary = "Activate/Deactivate trainee",
            description = "Change trainee's active status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Trainee not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> toggleTraineeStatus(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody ToggleActiveRequest request) {

        log.info("Toggling active status for trainee: {}", username);

        // Validate username matches
        if (!username.equals(request.getUsername())) {
            throw new com.epam.gym.exception.ValidationException(
                    "Username in path does not match username in request body");
        }

        gymFacade.toggleTraineeStatus(username, request.getPassword());

        log.info("Active status toggled for trainee: {}", username);
        return ResponseEntity.ok().build();
    }
}