package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingSpecification;
import com.epam.gym.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeRepository trainingTypeRepository;

    public Training createTraining(
            String traineeUsername, String traineePassword,
            String trainerUsername, String trainerPassword,
            String trainingName, TrainingTypeName trainingTypeName,
            LocalDate trainingDate, Integer duration) {

        log.info("Creating training: {} for trainee: {} and trainer: {}",
                trainingName, traineeUsername, trainerUsername);

        // Authenticate both trainee and trainer
        Trainee trainee = traineeService.selectByUsername(traineeUsername);
        traineeService.authenticate(traineeUsername, traineePassword);

        Trainer trainer = trainerService.selectByUsername(trainerUsername);
        trainerService.authenticate(trainerUsername, trainerPassword);

        // Get training type (entity)
        TrainingType trainingType = trainingTypeRepository
                .findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Training type not found: " + trainingTypeName));

        // Create training
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingTypeName)
                .trainingDate(trainingDate)
                .trainingDurationMinutes(duration)
                .build();

        Training saved = trainingRepository.save(training);
        log.info("Training created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(
            String traineeUsername, String traineePassword,
            LocalDate fromDate, LocalDate toDate,
            String trainerName, TrainingTypeName trainingTypeName) {

        log.info("Getting trainee trainings by criteria for: {}", traineeUsername);
        traineeService.authenticate(traineeUsername, traineePassword);

        return trainingRepository.findAll(
                TrainingSpecification.findTraineeTrainingsByCriteria(
                        traineeUsername, fromDate, toDate, trainerName, trainingTypeName));
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(
            String trainerUsername, String trainerPassword,
            LocalDate fromDate, LocalDate toDate,
            String traineeName) {

        log.info("Getting trainer trainings by criteria for: {}", trainerUsername);
        trainerService.authenticate(trainerUsername, trainerPassword);

        return trainingRepository.findAll(
                TrainingSpecification.findTrainerTrainingsByCriteria(
                        trainerUsername, fromDate, toDate, traineeName));
    }
}