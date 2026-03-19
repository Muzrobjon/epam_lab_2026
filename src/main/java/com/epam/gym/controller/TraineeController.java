package com.epam.gym.controller;

import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
import com.epam.gym.service.TraineeService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management", description = "Trainee profile and training management APIs")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final UserService userService;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;
    private final TrainingMapper trainingMapper;
    private final UserMapper userMapper;

    @Operation(summary = "Register trainee", description = "Create a new trainee profile")
    @PostMapping
    public ResponseEntity<RegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request) {

        log.info("Registering trainee: {} {}", request.getFirstName(), request.getLastName());

        Trainee trainee = traineeService.createProfile(request);

        RegistrationResponse response = userMapper.toRegistrationResponse(trainee);

        log.info("Trainee registered successfully: {}", response.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestHeader("X-Password") String password) {

        log.info("Fetching trainee profile: {}", username);

        traineeService.authenticate(username, password);
        Trainee trainee = traineeService.getByUsername(username);

        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        log.info("Trainee profile retrieved: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile", description = "Update trainee profile information")
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {

        log.info("Updating trainee profile: {}", username);

        if (!username.equals(request.getUsername())) {
            throw new ValidationException("Username in path does not match username in request body");
        }

        Trainee updated = traineeService.updateProfile(username, request);

        TraineeProfileResponse response = traineeMapper.toProfileResponse(updated);

        log.info("Trainee profile updated: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Delete trainee profile and associated trainings")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestHeader("X-Password") String password) {

        log.info("Deleting trainee profile: {}", username);

        traineeService.deleteByUsername(username, password);

        log.info("Trainee profile deleted: {}", username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate/Deactivate trainee", description = "Change trainee's active status")
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> toggleTraineeStatus(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody ToggleActiveRequest request) {

        log.info("Toggling active status for trainee: {}", username);

        if (!username.equals(request.getUsername())) {
            throw new ValidationException("Username in path does not match username in request body");
        }

        userService.setActiveStatus(username, request.getPassword(), request.getIsActive());

        log.info("Active status changed for trainee: {}", username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update trainee's trainers list", description = "Update the list of trainers assigned to a trainee")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryResponse>> updateTraineeTrainersList(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        log.info("Updating trainers list for trainee: {}", username);

        if (!username.equals(request.getTraineeUsername())) {
            throw new ValidationException("Username in path does not match username in request body");
        }

        List<Trainer> trainers = traineeService.updateTrainersList(
                request.getTraineeUsername(),
                request.getPassword(),
                request.getTrainerUsernames()
        );

        List<TrainerSummaryResponse> response = trainerMapper.toSummaryResponseList(trainers);

        log.info("Trainers list updated for trainee: {}", username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainee trainings", description = "Get trainee's training list with optional filters")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponse>> getTraineeTrainings(
            @Parameter(description = "Username of the trainee", required = true)
            @PathVariable String username,
            @Parameter(description = "Password for authentication", required = true)
            @RequestHeader("X-Password") String password,
            @Parameter(description = "Filter by start date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter by end date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter by trainer name")
            @RequestParam(required = false) String trainerName,
            @Parameter(description = "Filter by training type")
            @RequestParam(required = false) TrainingTypeName trainingType) {

        log.info("Fetching trainings for trainee: {}", username);

        List<Training> trainings = trainingService.getTraineeTrainingsByCriteria(
                username, password, fromDate, toDate, trainerName, trainingType
        );

        List<TrainingResponse> response = trainingMapper.toResponseList(trainings);

        log.info("Found {} trainings for trainee: {}", response.size(), username);
        return ResponseEntity.ok(response);
    }
}