package com.epam.gym.facade;

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

    /**
     * 1. Create Trainee profile
     */
    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        return traineeService.createProfile(firstName, lastName, dateOfBirth, address);
    }

    /**
     * 3. Trainee username and password matching
     */
    public void authenticateTrainee(String username, String password) {
        traineeService.authenticate(username, password);
    }

    /**
     * 6. Select Trainee profile by username
     */
    public Trainee getTraineeByUsername(String username) {
        return traineeService.selectByUsername(username);
    }

    /**
     * 7. Trainee password change
     */
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    /**
     * 10. Update trainee profile
     */
    public Trainee updateTrainee(String username, String password, Trainee trainee) {
        return traineeService.updateProfile(username, password, trainee);
    }

    /**
     * 11. Activate/De-activate trainee
     */
    public void toggleTraineeStatus(String username, String password) {
        traineeService.toggleActiveStatus(username, password);
    }

    /**
     * 13. Delete trainee profile by username
     */
    public void deleteTrainee(String username, String password) {
        traineeService.deleteByUsername(username, password);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.findAll();
    }

    // ==================== TRAINER OPERATIONS ====================

    /**
     * 2. Create Trainer profile
     */
    public Trainer createTrainer(String firstName, String lastName, String specialization) {
        return trainerService.createProfile(firstName, lastName, specialization);
    }

    /**
     * 4. Trainer username and password matching
     */
    public void authenticateTrainer(String username, String password) {
        trainerService.authenticate(username, password);
    }

    /**
     * 5. Select Trainer profile by username
     */
    public Trainer getTrainerByUsername(String username) {
        return trainerService.selectByUsername(username);
    }

    /**
     * 8. Trainer password change
     */
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    /**
     * 9. Update trainer profile
     */
    public Trainer updateTrainer(String username, String password, Trainer trainer) {
        return trainerService.updateProfile(username, password, trainer);
    }

    /**
     * 12. Activate/De-activate trainer
     */
    public void toggleTrainerStatus(String username, String password) {
        trainerService.toggleActiveStatus(username, password);
    }

    /**
     * 17. Get trainers list that not assigned on trainee by trainee's username
     */
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerService.getUnassignedTrainers(traineeUsername);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.findAll();
    }

    public Training createTraining(String traineeUsername, String traineePassword,
                                   String trainerUsername, String trainerPassword,
                                   String trainingName, String trainingTypeName,
                                   LocalDate trainingDate, Integer duration) {
        return trainingService.createTraining(traineeUsername, traineePassword,
                trainerUsername, trainerPassword, trainingName, trainingTypeName,
                trainingDate, duration);
    }

    public List<Training> getTraineeTrainingsByCriteria(String traineeUsername, String traineePassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String trainerName, String trainingTypeName) {
        return trainingService.getTraineeTrainingsByCriteria(traineeUsername, traineePassword,
                fromDate, toDate, trainerName, trainingTypeName);
    }

    public List<Training> getTrainerTrainingsByCriteria(String trainerUsername, String trainerPassword,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        String traineeName) {
        return trainingService.getTrainerTrainingsByCriteria(trainerUsername, trainerPassword,
                fromDate, toDate, traineeName);
    }

    public void updateTraineeTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
        traineeService.updateTrainersList(traineeUsername, password, trainerUsernames);
    }

    public List<Training> getAllTrainings() {
        return trainingService.findAll();
    }
}
