package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingRepository trainingRepository;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingTypeRepository trainingTypeRepository;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);

        trainingService = new TrainingService(
                trainingRepository,
                traineeService,
                trainerService,
                trainingTypeRepository
        );
    }

    @Test
    void testCreateTraining_Success() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        String trainerUsername = "trainer1";
        String trainerPassword = "pass2";
        String trainingName = "Morning Cardio";
        TrainingTypeName trainingTypeName = TrainingTypeName.CARDIO;
        LocalDate trainingDate = LocalDate.of(2024, 3, 10);
        Integer duration = 60;

        Trainee trainee = Trainee.builder().userName(traineeUsername).password(traineePassword).build();
        Trainer trainer = Trainer.builder().userName(trainerUsername).password(trainerPassword).build();
        TrainingType trainingType = new TrainingType(1L, trainingTypeName);

        when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName)).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        Training result = trainingService.createTraining(
                traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingTypeName,
                trainingDate, duration
        );

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(trainingName, result.getTrainingName());
        assertEquals(trainingTypeName, result.getTrainingType());
        assertEquals(trainingDate, result.getTrainingDate());
        assertEquals(duration, result.getTrainingDurationMinutes());
    }

    @Test
    void testCreateTraining_TrainingTypeNotFound() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        String trainerUsername = "trainer1";
        String trainerPassword = "pass2";
        String trainingName = "Morning Cardio";
        TrainingTypeName trainingTypeName = TrainingTypeName.CARDIO;
        LocalDate trainingDate = LocalDate.of(2024, 3, 10);
        Integer duration = 60;

        Trainee trainee = Trainee.builder().userName(traineeUsername).password(traineePassword).build();
        Trainer trainer = Trainer.builder().userName(trainerUsername).password(trainerPassword).build();

        when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                trainingService.createTraining(
                        traineeUsername, traineePassword,
                        trainerUsername, trainerPassword,
                        trainingName, trainingTypeName,
                        trainingDate, duration
                ));
    }

    @Test
    void testGetTraineeTrainingsByCriteria() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        LocalDate fromDate = LocalDate.of(2024, 3, 1);
        LocalDate toDate = LocalDate.of(2024, 3, 31);
        String trainerName = "trainer";
        TrainingTypeName trainingTypeName = TrainingTypeName.CARDIO;

        List<Training> trainings = List.of(
                Training.builder().trainingName("Morning Cardio").build()
        );

        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        when(trainingRepository.findAll((Specification<Training>) any())).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingTypeName
        );
        assertEquals(trainings, result);
    }

    @Test
    void testGetTrainerTrainingsByCriteria() {
        String trainerUsername = "trainer1";
        String trainerPassword = "pass1";
        LocalDate fromDate = LocalDate.of(2024, 3, 1);
        LocalDate toDate = LocalDate.of(2024, 3, 31);
        String traineeName = "trainee";

        List<Training> trainings = List.of(
                Training.builder().trainingName("Evening Strength").build()
        );

        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(trainingRepository.findAll((Specification<Training>) any())).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                trainerUsername, trainerPassword, fromDate, toDate, traineeName
        );
        assertEquals(trainings, result);
    }
}