package com.epam.gym.facade;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.updateProfile(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.deleteProfile(id);
    }

    public Optional<Trainee> getTrainee(Long id) {
        return traineeService.selectProfile(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.findAll();
    }

    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        return trainerService.updateProfile(trainer);
    }

    public Optional<Trainer> getTrainer(Long id) {
        return trainerService.selectProfile(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.findAll();
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate,
                                   Integer duration) {
        return trainingService.createTraining(traineeId, trainerId, trainingName,
                trainingType, trainingDate, duration);
    }

    public Optional<Training> getTraining(Long id) {
        return trainingService.selectProfile(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.findAll();
    }
}
