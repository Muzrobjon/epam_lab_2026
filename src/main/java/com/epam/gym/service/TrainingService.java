package com.epam.gym.service;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
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
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final Validator validator;

    @Transactional
    public Training createTraining(String traineeUsername, String traineePassword,
                                   String trainerUsername, String trainerPassword,
                                   String trainingName, String trainingTypeName,
                                   LocalDate trainingDate, Integer duration) {
        log.info("Creating training: {} for trainee {} and trainer {}",
                trainingName, traineeUsername, trainerUsername);

        traineeService.authenticate(traineeUsername, traineePassword);
        trainerService.authenticate(trainerUsername, trainerPassword);

        Trainee trainee = traineeRepository.findByUserName(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + traineeUsername));

        Trainer trainer = trainerRepository.findByUserName(trainerUsername)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + trainerUsername));

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> new NotFoundException("Training type not found: " + trainingTypeName));

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDurationMinutes(duration)
                .build();

        validateTraining(training);

        Training saved = trainingRepository.save(training);
        log.info("Created training: {} (ID: {})", saved.getTrainingName(), saved.getTrainingId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(String traineeUsername, String traineePassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String trainerName, String trainingTypeName) {
        log.info("Getting trainee trainings by criteria for: {}", traineeUsername);

        traineeService.authenticate(traineeUsername, traineePassword);

        return trainingRepository.findTraineeTrainingsByCriteria(
                traineeUsername, fromDate, toDate, trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(String trainerUsername, String trainerPassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String traineeName) {
        log.info("Getting trainer trainings by criteria for: {}", trainerUsername);

        trainerService.authenticate(trainerUsername, trainerPassword);

        return trainingRepository.findTrainerTrainingsByCriteria(
                trainerUsername, fromDate, toDate, traineeName);
    }

    @Transactional(readOnly = true)
    public Training selectProfile(Long id) {
        return trainingRepository.findAll().stream()
                .filter(t -> t.getTrainingId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Training not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Training> findByTraineeUsername(String traineeUsername) {
        return trainingRepository.findByTraineeUsername(traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<Training> findByTrainerUsername(String trainerUsername) {
        return trainingRepository.findByTrainerUsername(trainerUsername);
    }

    private void validateTraining(Training training) {
        Set<ConstraintViolation<Training>> violations = validator.validate(training);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }
}
