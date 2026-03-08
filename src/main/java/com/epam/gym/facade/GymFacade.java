package com.epam.gym.facade;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    // Trainee operations
    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    public void authenticateTrainee(String username, String password) {
        traineeService.authenticate(username, password);
    }

    public Trainee getTraineeByUsername(String username) {
        return traineeService.selectByUsername(username);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public Trainee updateTrainee(String username, String password, Trainee updatedTrainee) {
        return traineeService.updateProfile(username, password, updatedTrainee);
    }

    public void toggleTraineeStatus(String username, String password) {
        traineeService.toggleActiveStatus(username, password);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }

    public void updateTraineeTrainersList(String traineeUsername, String password,
                                          List<String> trainerUsernames) {
        traineeService.updateTrainersList(traineeUsername, password, trainerUsernames);
    }

    // Trainer operations
    public Trainer createTrainer(String firstName, String lastName, TrainingTypeName specialization) {
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    public void authenticateTrainer(String username, String password) {
        trainerService.authenticate(username, password);
    }

    public Trainer getTrainerByUsername(String username) {
        return trainerService.selectByUsername(username);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerService.getUnassignedTrainers(traineeUsername);
    }

    // Training operations
    public Training createTraining(String traineeUsername, String traineePassword,
                                   String trainerUsername, String trainerPassword,
                                   String trainingName, TrainingTypeName trainingTypeName,
                                   LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingTypeName,
                trainingDate, duration);
    }

    public List<Training> getTraineeTrainingsByCriteria(String traineeUsername, String traineePassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String trainerName, TrainingTypeName trainingTypeName) {
        return trainingService.getTraineeTrainingsByCriteria(traineeUsername, traineePassword,
                fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainingsByCriteria(String trainerUsername, String trainerPassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String traineeName) {
        return trainingService.getTrainerTrainingsByCriteria(trainerUsername, trainerPassword,
                fromDate, toDate, traineeName);
    }
}