package com.epam.gym.facade;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymFacadeTest {

    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingService trainingService;
    private GymFacade gymFacade;

    @BeforeEach
    void setUp() {
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingService = mock(TrainingService.class);
        gymFacade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void testCreateTrainee() {
        Trainee trainee = new Trainee();
        when(traineeService.createProfile("John", "Doe", LocalDate.of(2000, 1, 1), "Address"))
                .thenReturn(trainee);

        Trainee result = gymFacade.createTrainee("John", "Doe", LocalDate.of(2000, 1, 1), "Address");
        assertEquals(trainee, result);
        verify(traineeService).createProfile("John", "Doe", LocalDate.of(2000, 1, 1), "Address");
    }

    @Test
    void testAuthenticateTrainee() {
        gymFacade.authenticateTrainee("user", "pass");
        verify(traineeService).authenticate("user", "pass");
    }

    @Test
    void testGetTraineeByUsername() {
        Trainee trainee = new Trainee();
        when(traineeService.selectByUsername("user")).thenReturn(trainee);

        Trainee result = gymFacade.getTraineeByUsername("user");
        assertEquals(trainee, result);
        verify(traineeService).selectByUsername("user");
    }

    @Test
    void testChangeTraineePassword() {
        gymFacade.changeTraineePassword("user", "old", "new");
        verify(traineeService).changePassword("user", "old", "new");
    }

    @Test
    void testUpdateTrainee() {
        Trainee updated = new Trainee();
        when(traineeService.updateProfile("user", "pass", updated)).thenReturn(updated);

        Trainee result = gymFacade.updateTrainee("user", "pass", updated);
        assertEquals(updated, result);
        verify(traineeService).updateProfile("user", "pass", updated);
    }

    @Test
    void testToggleTraineeStatus() {
        gymFacade.toggleTraineeStatus("user", "pass");
        verify(traineeService).toggleActiveStatus("user", "pass");
    }

    @Test
    void testDeleteTrainee() {
        gymFacade.deleteTrainee("user", "pass");
        verify(traineeService).deleteByUsername("user", "pass");
    }

    @Test
    void testUpdateTraineeTrainersList() {
        List<String> trainers = Arrays.asList("trainer1", "trainer2");
        gymFacade.updateTraineeTrainersList("user", "pass", trainers);
        verify(traineeService).updateTrainersList("user", "pass", trainers);
    }

    @Test
    void testCreateTrainer() {
        Trainer trainer = new Trainer();
        when(trainerService.createProfile("Jane", "Smith", TrainingTypeName.CARDIO)).thenReturn(trainer);

        Trainer result = gymFacade.createTrainer("Jane", "Smith", TrainingTypeName.CARDIO);
        assertEquals(trainer, result);
        verify(trainerService).createProfile("Jane", "Smith", TrainingTypeName.CARDIO);
    }

    @Test
    void testAuthenticateTrainer() {
        gymFacade.authenticateTrainer("trainer", "pass");
        verify(trainerService).authenticate("trainer", "pass");
    }

    @Test
    void testGetTrainerByUsername() {
        Trainer trainer = new Trainer();
        when(trainerService.selectByUsername("trainer")).thenReturn(trainer);

        Trainer result = gymFacade.getTrainerByUsername("trainer");
        assertEquals(trainer, result);
        verify(trainerService).selectByUsername("trainer");
    }

    @Test
    void testGetUnassignedTrainers() {
        List<Trainer> trainers = Collections.singletonList(new Trainer());
        when(trainerService.getUnassignedTrainers("trainee")).thenReturn(trainers);

        List<Trainer> result = gymFacade.getUnassignedTrainers("trainee");
        assertEquals(trainers, result);
        verify(trainerService).getUnassignedTrainers("trainee");
    }

    @Test
    void testCreateTraining() {
        Training training = new Training();
        when(trainingService.createTraining(
                "trainee", "tpass", "trainer", "trpass",
                "Morning Cardio", TrainingTypeName.CARDIO,
                LocalDate.of(2024, 1, 1), 60))
                .thenReturn(training);

        Training result = gymFacade.createTraining(
                "trainee", "tpass", "trainer", "trpass",
                "Morning Cardio", TrainingTypeName.CARDIO,
                LocalDate.of(2024, 1, 1), 60);

        assertEquals(training, result);
        verify(trainingService).createTraining(
                "trainee", "tpass", "trainer", "trpass",
                "Morning Cardio", TrainingTypeName.CARDIO,
                LocalDate.of(2024, 1, 1), 60);
    }

    @Test
    void testGetTraineeTrainingsByCriteria() {
        List<Training> trainings = Collections.singletonList(new Training());
        when(trainingService.getTraineeTrainingsByCriteria(
                "trainee", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "trainer", TrainingTypeName.CARDIO)).thenReturn(trainings);

        List<Training> result = gymFacade.getTraineeTrainingsByCriteria(
                "trainee", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "trainer", TrainingTypeName.CARDIO);

        assertEquals(trainings, result);
        verify(trainingService).getTraineeTrainingsByCriteria(
                "trainee", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2,
                        1),
                "trainer", TrainingTypeName.CARDIO);
    }

    @Test
    void testGetTrainerTrainingsByCriteria() {
        List<Training> trainings = Collections.singletonList(new Training());
        when(trainingService.getTrainerTrainingsByCriteria(
                "trainer", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "trainee")).thenReturn(trainings);

        List<Training> result = gymFacade.getTrainerTrainingsByCriteria(
                "trainer", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "trainee");

        assertEquals(trainings, result);
        verify(trainingService).getTrainerTrainingsByCriteria(
                "trainer", "pass", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1),
                "trainee");
    }
}