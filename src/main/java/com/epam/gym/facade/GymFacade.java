package com.epam.gym.facade;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public Trainer createTrainer(String firstName, String lastName, TrainingTypeName specialization) {
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    public void authenticateTrainee(String username, String password) {
        traineeService.authenticate(username, password);
    }

    public void authenticateTrainer(String username, String password) {
        trainerService.authenticate(username, password);
    }

    public Trainer getTrainerByUsername(String username) {
        return trainerService.selectByUsername(username);
    }

    public Trainee getTraineeByUsername(String username) {
        return traineeService.selectByUsername(username);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public Trainer updateTrainer(String username, String password, Trainer updatedTrainer) {
        return trainerService.updateProfile(username, password, updatedTrainer);
    }

    public Trainee updateTrainee(String username, String password, Trainee updatedTrainee) {
        return traineeService.updateProfile(username, password, updatedTrainee);
    }

    public void toggleTraineeStatus(String username, String password) {
        traineeService.toggleActiveStatus(username, password);
    }

    public void toggleTrainerStatus(String username, String password) {
        trainerService.toggleActiveStatus(username, password);
    }

    public void deleteTrainee(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }

    public List<Training> getTraineeTrainingsByCriteria(
            String traineeUsername, String traineePassword,
            LocalDate fromDate, LocalDate toDate,
            String trainerName, TrainingTypeName trainingType) {
        return trainingService.getTraineeTrainingsByCriteria(
                traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingType);
    }

    public List<Training> getTrainerTrainingsByCriteria(
            String trainerUsername, String trainerPassword,
            LocalDate fromDate, LocalDate toDate,
            String traineeName) {
        return trainingService.getTrainerTrainingsByCriteria(
                trainerUsername, trainerPassword, fromDate, toDate, traineeName);
    }

    public Training createTraining(
            String traineeUsername, String traineePassword,
            String trainerUsername, String trainerPassword,
            String trainingName, TrainingTypeName trainingType,
            LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(
                traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingType, trainingDate, duration);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerService.getUnassignedTrainers(traineeUsername);
    }

    public void updateTraineeTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
        traineeService.updateTrainersList(traineeUsername, password, trainerUsernames);
    }
}