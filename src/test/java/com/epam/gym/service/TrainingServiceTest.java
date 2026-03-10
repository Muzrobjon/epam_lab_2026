package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TrainingRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private Validator validator;

    @InjectMocks
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingService = new TrainingService(
                trainingRepository,
                traineeService,
                trainerService,
                validator
        );
    }

    @Test
    void createTraining_success() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        String trainerUsername = "trainer1";
        String trainerPassword = "pass2";
        String trainingName = "Morning Yoga";
        TrainingTypeName trainingType = TrainingTypeName.YOGA;
        LocalDate trainingDate = LocalDate.now();
        Integer duration = 60;

        User traineeUser = User.builder().username(traineeUsername).build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        User trainerUser = User.builder().username(trainerUsername).build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();

        Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDurationMinutes(duration)
                .build();

        Training savedTraining = Training.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDurationMinutes(duration)
                .build();

        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
        when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
        when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
        when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);

        Training
                result = trainingService.createTraining(
                traineeUsername, traineePassword,
                trainerUsername, trainerPassword,
                trainingName, trainingType,
                trainingDate, duration
        );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(trainingName, result.getTrainingName());
        assertEquals(trainingType, result.getTrainingType());
        assertEquals(trainingDate, result.getTrainingDate());
        assertEquals(duration, result.getTrainingDurationMinutes());

        verify(traineeService).authenticate(traineeUsername, traineePassword);
        verify(trainerService).authenticate(trainerUsername, trainerPassword);
        verify(traineeService).selectByUsername(traineeUsername);
        verify(trainerService).selectByUsername(trainerUsername);
        verify(validator).validate(any(Training.class));
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createTraining_validationFails_throwsException() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        String trainerUsername = "trainer1";
        String trainerPassword = "pass2";
        String trainingName = "Morning Yoga";
        TrainingTypeName trainingType = TrainingTypeName.YOGA;
        LocalDate trainingDate = LocalDate.now();
        Integer duration = 60;

        User traineeUser = User.builder().username(traineeUsername).build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        User trainerUser = User.builder().username(trainerUsername).build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();

        ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid training");

        Set<ConstraintViolation<Training>> violations = new HashSet<>();
        violations.add(violation);

        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
        when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
        when(validator.validate(any(Training.class))).thenReturn(violations);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                trainingService.createTraining(
                        traineeUsername, traineePassword,
                        trainerUsername, trainerPassword,
                        trainingName, trainingType,
                        trainingDate, duration
                )
        );
        assertTrue(ex.getMessage().contains("Invalid training"));
    }

    @Test
    void getTraineeTrainingsByCriteria_filtersByTrainerNameAndType() {
        String traineeUsername = "trainee1";
        String traineePassword = "pass1";
        LocalDate fromDate = LocalDate.now().minusDays(10);
        LocalDate toDate = LocalDate.now().plusDays(10);
        String trainerName = "John";
        TrainingTypeName trainingType = TrainingTypeName.YOGA;

        User trainerUser = User.builder().firstName("John").lastName("Doe").build();
        Trainer trainer = Trainer.builder().user(trainerUser).build();
        Training training = Training.builder()
                .trainer(trainer)
                .trainingType(trainingType)
                .build();

        List<Training> trainings = Collections.singletonList(training);

        doNothing().when(traineeService).authenticate(traineeUsername, traineePassword);
        when(trainingRepository.findTrainingsWithAllUsers(traineeUsername, null, fromDate, toDate))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingType
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(trainingType, result.getFirst().getTrainingType());
        assertEquals("John", result.getFirst().getTrainer().getUser().getFirstName());
    }

    @Test
    void getTrainerTrainingsByCriteria_filtersByTraineeName() {
        String trainerUsername = "trainer1";
        String trainerPassword = "pass1";
        LocalDate fromDate = LocalDate.now().minusDays(10);
        LocalDate toDate = LocalDate.now().plusDays(10
        );
        String traineeName = "Alice";

        User traineeUser = User.builder().firstName("Alice").lastName("Smith").build();
        Trainee trainee = Trainee.builder().user(traineeUser).build();
        Training training = Training.builder()
                .trainee(trainee)
                .build();

        List<Training> trainings = Collections.singletonList(training);

        doNothing().when(trainerService).authenticate(trainerUsername, trainerPassword);
        when(trainingRepository.findTrainingsWithAllUsers(null, trainerUsername, fromDate, toDate))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                trainerUsername, trainerPassword, fromDate, toDate, traineeName
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.getFirst().getTrainee().getUser().getFirstName());
    }

    @Test
    void getTrainingsWithAllUsers_returnsList() {
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        LocalDate fromDate = LocalDate.now().minusDays(10);
        LocalDate toDate = LocalDate.now().plusDays(10);

        Training training = Training.builder().build();
        when(trainingRepository.findTrainingsWithAllUsers(traineeUsername, trainerUsername, fromDate, toDate))
                .thenReturn(Collections.singletonList(training));

        List<Training> result = trainingService.getTrainingsWithAllUsers(
                traineeUsername, trainerUsername, fromDate, toDate
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}