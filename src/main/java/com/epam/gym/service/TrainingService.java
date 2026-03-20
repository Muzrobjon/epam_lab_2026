package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final Validator validator;
    private final UserService userService;

    @Transactional
    public void createTraining(AddTrainingRequest request) {
        userService.isAuthenticated(request.getTraineeUsername());
        log.info("Creating training: {} for trainee: {} and trainer: {}",
                request.getTrainingName(),
                request.getTraineeUsername(),
                request.getTrainerUsername());


        Trainee trainee = traineeService.getByUsername(request.getTraineeUsername());
        Trainer trainer = trainerService.getByUsername(request.getTrainerUsername());

        TrainingType trainingType = trainer.getSpecialization();

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainingType)
                .trainingDate(request.getTrainingDate())
                .trainingDurationMinutes(request.getTrainingDuration())
                .build();

        validateTraining(training);

        Training saved = trainingRepository.save(training);
        log.info("Training created successfully with ID: {}", saved.getId());

    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate, LocalDate toDate,
            String trainerName, TrainingTypeName trainingTypeName) {

        log.info("Getting trainee trainings by criteria for: {}", traineeUsername);
        userService.isAuthenticated(traineeUsername);

        List<Training> trainings = trainingRepository.findTrainingsWithAllUsers(
                traineeUsername, null, fromDate, toDate);

        if (trainerName != null && !trainerName.isBlank()) {
            String lowerName = trainerName.toLowerCase();
            trainings = trainings.stream()
                    .filter(t -> t.getTrainer().getUser().getFirstName().toLowerCase().contains(lowerName) ||
                            t.getTrainer().getUser().getLastName().toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
        }

        if (trainingTypeName != null) {
            trainings = trainings.stream()
                    .filter(t -> t.getTrainingType().getTrainingTypeName() == trainingTypeName)
                    .collect(Collectors.toList());
        }

        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate, LocalDate toDate,
            String traineeName) {

        log.info("Getting trainer trainings by criteria for: {}", trainerUsername);
        userService.isAuthenticated(trainerUsername);

        List<Training> trainings = trainingRepository.findTrainingsWithAllUsers(
                null, trainerUsername, fromDate, toDate);

        if (traineeName != null && !traineeName.isBlank()) {
            String lowerName = traineeName.toLowerCase();
            trainings = trainings.stream()
                    .filter(t -> t.getTrainee().getUser().getFirstName().toLowerCase().contains(lowerName) ||
                            t.getTrainee().getUser().getLastName().toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
        }

        log.info("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        log.info("Fetching all training types");
        return trainingTypeRepository.findAll();
    }

    private void validateTraining(Training training) {
        Set<ConstraintViolation<Training>> violations = validator.validate(training);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Training validation failed: " + message);
        }
    }
}