package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
@Tag(name = "Training Management", description = "Training creation and type management APIs")
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingTypeMapper trainingTypeMapper;

    @Operation(summary = "Add training", description = "Create a new training session")
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        log.info("Adding training: {} for trainee: {} and trainer: {}",
                request.getTrainingName(),
                request.getTraineeUsername(),
                request.getTrainerUsername());

        trainingService.createTraining(request);

        log.info("Training created successfully: {}", request.getTrainingName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Retrieve all available training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        log.info("Fetching all training types");

        List<TrainingType> types = trainingService.getAllTrainingTypes();
        List<TrainingTypeResponse> response = trainingTypeMapper.toResponseList(types);

        log.info("Found {} training types", response.size());
        return ResponseEntity.ok(response);
    }
}