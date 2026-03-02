package com.epam.gym.facade;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymFacade gymFacade;

    private final LocalDate date = LocalDate.of(2024, 1, 1);

    // ==================== TRAINEE TESTS ====================

    @Test
    void shouldCreateTrainee() {
        Trainee trainee = new Trainee();
        when(traineeService.createProfile("John", "Doe", date, "Address"))
                .thenReturn(trainee);

        Trainee result = gymFacade.createTrainee("John", "Doe", date, "Address");

        assertEquals(trainee, result);
        verify(traineeService).createProfile("John", "Doe", date, "Address");
    }

    @Test
    void shouldAuthenticateTrainee() {
        gymFacade.authenticateTrainee("user", "pass");
        verify(traineeService).authenticate("user", "pass");
    }

    @Test
    void shouldGetTraineeByUsername() {
        Trainee trainee = new Trainee();
        when(traineeService.selectByUsername("user")).thenReturn(trainee);

        Trainee result = gymFacade.getTraineeByUsername("user");

        assertEquals(trainee, result);
        verify(traineeService).selectByUsername("user");
    }

    @Test
    void shouldDeleteTrainee() {
        gymFacade.deleteTrainee("user", "pass");
        verify(traineeService).deleteByUsername("user", "pass");
    }

    // ==================== TRAINER TESTS ====================

    @Test
    void shouldCreateTrainer() {
        Trainer trainer = new Trainer();
        when(trainerService.createProfile("John", "Smith", "Fitness"))
                .thenReturn(trainer);

        Trainer result = gymFacade.createTrainer("John", "Smith", "Fitness");

        assertEquals(trainer, result);
        verify(trainerService).createProfile("John", "Smith", "Fitness");
    }

    @Test
    void shouldGetTrainerByUsername() {
        Trainer trainer = new Trainer();
        when(trainerService.selectByUsername("trainer")).thenReturn(trainer);

        Trainer result = gymFacade.getTrainerByUsername("trainer");

        assertEquals(trainer, result);
        verify(trainerService).selectByUsername("trainer");
    }

    @Test
    void shouldGetUnassignedTrainers() {
        List<Trainer> trainers = List.of(new Trainer());
        when(trainerService.getUnassignedTrainers("trainee"))
                .thenReturn(trainers);

        List<Trainer> result = gymFacade.getUnassignedTrainers("trainee");

        assertEquals(trainers, result);
        verify(trainerService).getUnassignedTrainers("trainee");
    }

    // ==================== TRAINING TESTS ====================

    @Test
    void shouldCreateTraining() {
        Training training = new Training();

        when(trainingService.createTraining(
                "tUser", "tPass",
                "trUser", "trPass",
                "Morning Workout", "Cardio",
                date, 60))
                .thenReturn(training);

        Training result = gymFacade.createTraining(
                "tUser", "tPass",
                "trUser", "trPass",
                "Morning Workout", "Cardio",
                date, 60);

        assertEquals(training, result);

        verify(trainingService).createTraining(
                "tUser", "tPass",
                "trUser", "trPass",
                "Morning Workout", "Cardio",
                date, 60);
    }

    @Test
    void shouldGetAllTrainings() {
        List<Training> trainings = List.of(new Training());
        when(trainingService.findAll()).thenReturn(trainings);

        List<Training> result = gymFacade.getAllTrainings();

        assertEquals(trainings, result);
        verify(trainingService).findAll();
    }
}