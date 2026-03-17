package com.epam.gym.facade;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

        @Transactional
    public Trainer createTrainer(String firstName, String lastName, TrainingTypeName specialization) {
        log.debug("Facade: Creating trainer {} {} with specialization {}",
                firstName, lastName, specialization);
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(String username) {
        log.debug("Facade: Getting trainer by username: {}", username);
        return trainerService.selectByUsername(username);
    }

    @Transactional
    public Trainer updateTrainer(String username, String password, Trainer updatedTrainer) {
        log.debug("Facade: Updating trainer profile: {}", username);
        return trainerService.updateProfile(username, password, updatedTrainer);
    }

    public void authenticateTrainer(String username, String password) {
        log.debug("Facade: Authenticating trainer: {}", username);
        trainerService.authenticate(username, password);
    }


    @Transactional
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        log.debug("Facade: Changing password for trainer: {}", username);
        trainerService.changePassword(username, oldPassword, newPassword);
    }


    @Transactional
    public void toggleTrainerStatus(String username, String password) {
        log.debug("Facade: Toggling active status for trainer: {}", username);
        trainerService.toggleActiveStatus(username, password);
    }


    @Transactional
    public void setTrainerStatus(String username, String password, Boolean isActive) {
        log.debug("Facade: Setting active status for trainer: {} to {}", username, isActive);
        trainerService.setActiveStatus(username, password, isActive);
    }


    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.debug("Facade: Getting unassigned trainers for trainee: {}", traineeUsername);
        return trainerService.getUnassignedTrainers(traineeUsername);
    }


    @Transactional
    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.debug("Facade: Creating trainee {} {} with DOB {} and address {}",
                firstName, lastName, dateOfBirth, address);
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    @Transactional(readOnly = true)
    public Trainee getTraineeByUsername(String username) {
        log.debug("Facade: Getting trainee by username: {}", username);
        return traineeService.selectByUsername(username);
    }


    @Transactional
    public Trainee updateTrainee(String username, String password, Trainee updatedTrainee) {
        log.debug("Facade: Updating trainee profile: {}", username);
        return traineeService.updateProfile(username, password, updatedTrainee);
    }


    @Transactional
    public void deleteTrainee(String username, String password) {
        log.debug("Facade: Deleting trainee: {}", username);
        traineeService.deleteByUsername(username, password);
    }


    public void authenticateTrainee(String username, String password) {
        log.debug("Facade: Authenticating trainee: {}", username);
        traineeService.authenticate(username, password);
    }


    @Transactional
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        log.debug("Facade: Changing password for trainee: {}", username);
        traineeService.changePassword(username, oldPassword, newPassword);
    }


    @Transactional
    public void toggleTraineeStatus(String username, String password) {
        log.debug("Facade: Toggling active status for trainee: {}", username);
        traineeService.toggleActiveStatus(username, password);
    }


    @Transactional
    public void setTraineeStatus(String username, String password, Boolean isActive) {
        log.debug("Facade: Setting active status for trainee: {} to {}", username, isActive);
        traineeService.setActiveStatus(username, password, isActive);
    }


    @Transactional
    public void updateTraineeTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
        log.debug("Facade: Updating trainers list for trainee: {} with {} trainers",
                traineeUsername, trainerUsernames.size());
        traineeService.updateTrainersList(traineeUsername, password, trainerUsernames);
    }


    @Transactional
    public Training createTraining(
            String traineeUsername, String traineePassword,
            String trainerUsername, String trainerPassword,
            String trainingName, TrainingTypeName trainingType,
            LocalDate trainingDate, Integer duration) {

        log.debug("Facade: Creating training '{}' for trainee {} and trainer {} on {}",
                trainingName, traineeUsername, trainerUsername, trainingDate);

        return trainingService.createTraining(
                traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingType, trainingDate, duration);
    }


    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainingsByCriteria(
            String traineeUsername, String traineePassword,
            LocalDate fromDate, LocalDate toDate,
            String trainerName, TrainingTypeName trainingType) {

        log.debug("Facade: Getting trainings for trainee {} with filters: from={}, to={}, trainer={}, type={}",
                traineeUsername, fromDate, toDate, trainerName, trainingType);

        return trainingService.getTraineeTrainingsByCriteria(
                traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingType);
    }


    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainingsByCriteria(
            String trainerUsername, String trainerPassword,
            LocalDate fromDate, LocalDate toDate,
            String traineeName) {

        log.debug("Facade: Getting trainings for trainer {} with filters: from={}, to={}, trainee={}",
                trainerUsername, fromDate, toDate, traineeName);

        return trainingService.getTrainerTrainingsByCriteria(
                trainerUsername, trainerPassword, fromDate, toDate, traineeName);
    }
}