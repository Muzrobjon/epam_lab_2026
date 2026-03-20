package com.epam.gym.controller;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer Management", description = "Trainer profile and training management APIs")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    @Operation(summary = "Register trainer", description = "Create a new trainer profile")
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {

        log.info("Registering trainer: {} {}", request.getFirstName(), request.getLastName());

        Trainer trainer = trainerService.createProfile(request);

        RegistrationResponse response = userMapper.toRegistrationResponse(trainer);

        log.info("Trainer registered successfully: {}", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username) {

        log.info("Fetching trainer profile: {}", username);

        userService.isAuthenticated(username);
        Trainer trainer = trainerService.getByUsername(username);

        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        log.info("Trainer profile retrieved: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile", description = "Update trainer profile information")
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request) {

        log.info("Updating trainer profile: {}", username);

        userService.isAuthenticated(username);
        Trainer updated = trainerService.updateProfile(username, request);

        TrainerProfileResponse response = trainerMapper.toProfileResponse(updated);

        log.info("Trainer profile updated: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get unassigned trainers",
            description = "Get list of available trainers not assigned to a specific trainee")
    @GetMapping("/{traineeUsername}/trainers/unassigned")
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String traineeUsername)
    {

        log.info("Fetching unassigned trainers for trainee: {}", traineeUsername);

        List<Trainer> trainers = trainerService.getUnassignedTrainers(traineeUsername);
        List<TrainerSummaryResponse> response = trainerMapper.toSummaryResponseList(trainers);

        log.info("Found {} unassigned trainers for trainee: {}", response.size(), traineeUsername);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer trainings",
            description = "Get trainer's training list with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTrainerTrainings(
            @Parameter(description = "Username of the trainer", required = true)
            @PathVariable String username,
            @Parameter(description = "Filter by start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter by end date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainee name")
            @RequestParam(required = false) String traineeName) {

        log.info("Fetching trainings for trainer: {}", username);

        List<Training> trainings = trainingService.getTrainerTrainingsByCriteria(
                username, fromDate, toDate, traineeName
        );

        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);

        log.info("Found {} trainings for trainer: {}", response.size(), username);
        return ResponseEntity.ok(response);
    }
}