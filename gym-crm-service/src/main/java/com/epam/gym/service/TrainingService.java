package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.TrainerWorkloadRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ConflictException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.metrics.TrainingMetrics;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final int MAX_MINUTES_PER_MONTH = 44640; // 31 * 24 * 60

    private final WorkloadMessageProducer workloadMessageProducer;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final Validator validator;
    private final UserService userService;
    private final TrainingMetrics trainingMetrics;


    @Transactional
    public void createTraining(AddTrainingRequest request) {
        Timer.Sample timer = trainingMetrics.startTimer();

        userService.isAuthenticated(request.getTraineeUsername());

        log.info("Creating training: {} for trainee: {} and trainer: {}",
                request.getTrainingName(),
                request.getTraineeUsername(),
                request.getTrainerUsername());

        validateTrainingDuration(request.getTrainingDuration());

        Trainee trainee = traineeService.getByUsername(request.getTraineeUsername());
        Trainer trainer = trainerService.getByUsername(request.getTrainerUsername());
        TrainingType trainingType = trainer.getSpecialization();

        validateNoOverlappingSchedule(trainer, request.getTrainingDate());

        validateTrainerMonthlyWorkload(trainer, request.getTrainingDate(), request.getTrainingDuration());

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

        trainingMetrics.stopTimer(timer);
        trainingMetrics.incrementCreated();

        // Notify trainer workload service
        workloadMessageProducer.sendNotification(trainer, request.getTrainingDate(),
                request.getTrainingDuration(), TrainerWorkloadRequest.ActionType.ADD);

        log.info("Training created with ID: {}", saved.getId());
    }

    // ⭐ YANGI METHOD: Overlap check
    private void validateNoOverlappingSchedule(Trainer trainer, LocalDate trainingDate) {
        if (trainingRepository.existsByTrainerAndDate(trainer.getId(), trainingDate)) {
            String trainerUsername = trainer.getUser().getUsername();
            log.warn("Trainer '{}' already has a training scheduled on {}",
                    trainerUsername, trainingDate);
            throw new ConflictException(String.format(
                    "Trainer '%s' already has a training scheduled on %s",
                    trainerUsername, trainingDate
            ));
        }
    }

    private void validateTrainingDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            throw new ValidationException("Training duration must be positive");
        }
        if (duration > MAX_MINUTES_PER_MONTH) {
            throw new ValidationException(
                    "Training duration cannot exceed one month (" + MAX_MINUTES_PER_MONTH +
                            " minutes). Provided: " + duration
            );
        }
    }

    private void validateTrainerMonthlyWorkload(Trainer trainer, LocalDate trainingDate, Integer newDuration) {
        YearMonth targetMonth = YearMonth.from(trainingDate);
        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();

        String trainerUsername = trainer.getUser().getUsername();

        List<Training> monthTrainings = trainingRepository.findTrainingsWithAllUsers(
                null, trainerUsername, firstDay, lastDay
        );

        int currentTotal = monthTrainings.stream()
                .mapToInt(Training::getTrainingDurationMinutes)
                .sum();

        long newTotal = (long) currentTotal + newDuration;

        if (newTotal > MAX_MINUTES_PER_MONTH) {
            throw new ValidationException(String.format(
                    "Trainer '%s' monthly workload limit exceeded for %s. " +
                            "Current: %d min, New training: %d min, Total would be: %d min, Max allowed: %d min",
                    trainerUsername, targetMonth, currentTotal, newDuration, newTotal, MAX_MINUTES_PER_MONTH
            ));
        }

        log.debug("Trainer '{}' monthly workload check passed for {}: {}/{} minutes",
                trainerUsername, targetMonth, newTotal, MAX_MINUTES_PER_MONTH);
    }

    @Timed(value = "gym_training_fetch_trainee_seconds")
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingTypeName) {

        log.info("Fetching trainings for trainee: {}", traineeUsername);
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

    @Timed(value = "gym_training_fetch_trainer_seconds")
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        log.info("Fetching trainings for trainer: {}", trainerUsername);
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