package com.epam;

import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    private Trainee testTrainee;
    private Trainer testTrainer;
    private Training testTraining;

    @BeforeEach
    void setUp() {
        testTrainee = Trainee.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .build();

        testTrainer = Trainer.builder()
                .userId(1L)
                .firstName("Mike")
                .lastName("Johnson")
                .userName("mike.johnson")
                .specialization("Fitness")
                .build();

        testTraining = Training.builder()
                .trainingId(1L)
                .trainingName("Morning Fitness")
                .build();
    }

    // Trainee Tests
    @Test
    void createTrainee_ShouldDelegateToService() {
        LocalDate dob = LocalDate.of(1995, 3, 20);
        when(traineeService.createProfile("John", "Doe", dob, "123 Main St"))
                .thenReturn(testTrainee);

        Trainee result = gymFacade.createTrainee("John", "Doe", dob, "123 Main St");

        assertEquals(testTrainee, result);
        verify(traineeService).createProfile("John", "Doe", dob, "123 Main St");
    }

    @Test
    void updateTrainee_ShouldDelegateToService() {
        when(traineeService.updateProfile(testTrainee)).thenReturn(testTrainee);

        Trainee result = gymFacade.updateTrainee(testTrainee);

        assertEquals(testTrainee, result);
        verify(traineeService).updateProfile(testTrainee);
    }

    @Test
    void deleteTrainee_ShouldDelegateToService() {
        gymFacade.deleteTrainee(1L);

        verify(traineeService).deleteProfile(1L);
    }

    @Test
    void getTrainee_ShouldDelegateToService() {
        when(traineeService.selectProfile(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = gymFacade.getTrainee(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void getAllTrainees_ShouldDelegateToService() {
        List<Trainee> trainees = Arrays.asList(testTrainee);
        when(traineeService.findAll()).thenReturn(trainees);

        List<Trainee> result = gymFacade.getAllTrainees();

        assertEquals(1, result.size());
        verify(traineeService).findAll();
    }

    // Trainer Tests
    @Test
    void createTrainer_ShouldDelegateToService() {
        when(trainerService.createProfile("Alice", "Fitness", "Cardio"))
                .thenReturn(testTrainer);

        Trainer result = gymFacade.createTrainer("Alice", "Fitness", "Cardio");

        assertEquals(testTrainer, result);
        verify(trainerService).createProfile("Alice", "Fitness", "Cardio");
    }

    @Test
    void updateTrainer_ShouldDelegateToService() {
        when(trainerService.updateProfile(testTrainer)).thenReturn(testTrainer);

        Trainer result = gymFacade.updateTrainer(testTrainer);

        assertEquals(testTrainer, result);
        verify(trainerService).updateProfile(testTrainer);
    }

    @Test
    void getTrainer_ShouldDelegateToService() {
        when(trainerService.selectProfile(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = gymFacade.getTrainer(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void getAllTrainers_ShouldDelegateToService() {
        List<Trainer> trainers = Arrays.asList(testTrainer);
        when(trainerService.findAll()).thenReturn(trainers);

        List<Trainer> result = gymFacade.getAllTrainers();

        assertEquals(1, result.size());
        verify(trainerService).findAll();
    }

    // Training Tests
    @Test
    void createTraining_ShouldDelegateToService() {
        LocalDate date = LocalDate.now();
        TrainingType type = TrainingType.builder().trainingTypeName("Cardio").build();

        when(trainingService.createTraining(1L, 1L, "Cardio Blast", type, date, 45))
                .thenReturn(testTraining);

        Training result = gymFacade.createTraining(1L, 1L, "Cardio Blast", type, date, 45);

        assertEquals(testTraining, result);
        verify(trainingService).createTraining(1L, 1L, "Cardio Blast", type, date, 45);
    }

    @Test
    void getTraining_ShouldDelegateToService() {
        when(trainingService.selectProfile(1L)).thenReturn(Optional.of(testTraining));

        Optional<Training> result = gymFacade.getTraining(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    void getAllTrainings_ShouldDelegateToService() {
        List<Training> trainings = Arrays.asList(testTraining);
        when(trainingService.findAll()).thenReturn(trainings);

        List<Training> result = gymFacade.getAllTrainings();

        assertEquals(1, result.size());
        verify(trainingService).findAll();
    }
}
