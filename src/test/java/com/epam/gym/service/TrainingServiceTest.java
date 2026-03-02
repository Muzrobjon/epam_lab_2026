package com.epam.gym.service;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.*;
import com.epam.gym.repository.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private Validator validator;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        validator = mock(Validator.class);

        trainingService = new TrainingService(
                trainingRepository,
                traineeRepository,
                trainerRepository,
                trainingTypeRepository,
                traineeService,
                trainerService,
                validator
        );
    }

    @Test
    void createTraining_shouldCreateAndSaveTraining() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        String trainerUsername = "trainer1";
        String trainerPassword = "pass2";
        String trainingName = "Morning Yoga";
        String trainingTypeName = "Yoga";
        LocalDate date = LocalDate.of(2026, 3, 2);
        int duration = 60;

        Trainee trainee = Trainee.builder().userName(traineeUsername).build();
        Trainer trainer = Trainer.builder().userName(trainerUsername).build();
        TrainingType trainingType = TrainingType.builder().trainingTypeName(trainingTypeName).build();

        when(traineeRepository.findByUserName(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserName(trainerUsername)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName)).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());

        Training saved = trainingService.createTraining(
                traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingTypeName, date, duration
        );

        assertEquals(trainingName, saved.getTrainingName());
        assertEquals(trainee, saved.getTrainee());
        assertEquals(trainer, saved.getTrainer());
        assertEquals(trainingType, saved.getTrainingType());
        assertEquals(date, saved.getTrainingDate());
        assertEquals(duration, saved.getTrainingDurationMinutes());

        verify(traineeService).authenticate(traineeUsername, traineePassword);
        verify(trainerService).authenticate(trainerUsername, trainerPassword);
        verify(trainingRepository).save(saved);
    }

    @Test
    void createTraining_shouldThrowNotFoundException_whenTraineeMissing() {
        when(traineeRepository.findByUserName("trainee1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> trainingService.createTraining(
                        "trainee1", "pass1", "trainer1", "pass2",
                        "Yoga", "Yoga", LocalDate.now(), 60
                ));
    }

    @Test
    void createTraining_shouldThrowValidationException_whenInvalid() {
        Trainee trainee = Trainee.builder().userName("t1").build();
        Trainer trainer = Trainer.builder().userName("tr1").build();
        TrainingType trainingType = TrainingType.builder().trainingTypeName("Yoga").build();

        when(traineeRepository.findByUserName("t1")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserName("tr1")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(trainingType));

        ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid training");
        when(validator.validate(any(Training.class))).thenReturn(Set.of(violation));

        assertThrows(ValidationException.class,
                () -> trainingService.createTraining(
                        "t1", "pass", "tr1", "pass",
                        "Yoga Session", "Yoga", LocalDate.now(), 30
                ));
    }

    @Test
    void getTraineeTrainingsByCriteria_shouldCallRepository() {
        String traineeUsername = "trainee1";
        String password = "pass";
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 5);

        List<Training> trainings = List.of(new Training(), new Training());
        when(trainingRepository.findTraineeTrainingsByCriteria(
                traineeUsername, from, to, "trainer1", "Yoga"
        )).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                traineeUsername, password, from, to, "trainer1", "Yoga"
        );

        verify(traineeService).authenticate(traineeUsername, password);
        assertEquals(2, result.size());
    }

    @Test
    void getTrainerTrainingsByCriteria_shouldCallRepository() {
        String trainerUsername = "trainer1";
        String password = "pass";
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 3, 5);

        List<Training> trainings = List.of(new Training(), new Training(), new Training());
        when(trainingRepository.findTrainerTrainingsByCriteria(
                trainerUsername, from, to, "trainee1"
        )).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                trainerUsername, password, from, to, "trainee1"
        );

        verify(trainerService).authenticate(trainerUsername, password);
        assertEquals(3, result.size());
    }

    @Test
    void selectProfile_shouldReturnTrainingOrThrow() {
        Training t1 = Training.builder().trainingId(1L).build();
        Training t2 = Training.builder().trainingId(2L).build();
        when(trainingRepository.findAll()).thenReturn(List.of(t1, t2));

        Training result = trainingService.selectProfile(2L);
        assertEquals(t2, result);

        assertThrows(NotFoundException.class, () -> trainingService.selectProfile(99L));
    }

    @Test
    void findByTraineeUsername_shouldReturnList() {
        Training t1 = new Training();
        Training t2 = new Training();
        List<Training> list = List.of(t1, t2);

        when(trainingRepository.findByTraineeUsername("trainee1")).thenReturn(list);
        List<Training> result = trainingService.findByTraineeUsername("trainee1");

        assertEquals(2, result.size());
    }

    @Test
    void findByTrainerUsername_shouldReturnList() {
        Training t1 = new Training();
        List<Training> list = List.of(t1);

        when(trainingRepository.findByTrainerUsername("trainer1")).thenReturn(list);
        List<Training> result = trainingService.findByTrainerUsername("trainer1");

        assertEquals(1, result.size());
    }
}