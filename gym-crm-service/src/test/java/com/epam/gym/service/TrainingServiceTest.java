package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.request.TrainerWorkloadRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.metrics.TrainingMetrics;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock private WorkloadMessageProducer workloadMessageProducer;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingTypeRepository trainingTypeRepository;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private Validator validator;
    @Mock private UserService userService;
    @Mock private TrainingMetrics trainingMetrics;
    @Mock private Timer.Sample timerSample;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private AddTrainingRequest addRequest;

    @BeforeEach
    void setUp() {
        User traineeUser = new User();
        traineeUser.setUsername("jane.doe");
        traineeUser.setFirstName("Jane");
        traineeUser.setLastName("Doe");
        traineeUser.setIsActive(true);

        trainee = new Trainee();
        trainee.setUser(traineeUser);

        User trainerUser = new User();
        trainerUser.setUsername("john.trainer");
        trainerUser.setFirstName("John");
        trainerUser.setLastName("Trainer");
        trainerUser.setIsActive(true);

        trainingType = new TrainingType();
        trainingType.setTrainingTypeName(TrainingTypeName.FITNESS);

        trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);

        addRequest = AddTrainingRequest.builder()
                .traineeUsername("jane.doe")
                .trainerUsername("john.trainer")
                .trainingName("Morning Workout")
                .trainingDate(LocalDate.of(2024, 5, 15))
                .trainingDuration(60)
                .build();
    }

    @Test
    @DisplayName("createTraining: saves training and notifies workload service")
    void createTraining_success() {
        when(traineeService.getByUsername("jane.doe")).thenReturn(trainee);
        when(trainerService.getByUsername("john.trainer")).thenReturn(trainer);
        when(validator.validate(any(Training.class))).thenReturn(Set.of());
        when(trainingMetrics.startTimer()).thenReturn(timerSample);
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        trainingService.createTraining(addRequest);

        // Verify authentication called
        verify(userService).isAuthenticated("jane.doe");

        // Verify training saved with correct fields
        ArgumentCaptor<Training> trainingCaptor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(trainingCaptor.capture());
        Training saved = trainingCaptor.getValue();
        assertThat(saved.getTrainee()).isEqualTo(trainee);
        assertThat(saved.getTrainer()).isEqualTo(trainer);
        assertThat(saved.getTrainingName()).isEqualTo("Morning Workout");
        assertThat(saved.getTrainingType()).isEqualTo(trainingType);
        assertThat(saved.getTrainingDate()).isEqualTo(LocalDate.of(2024, 5, 15));
        assertThat(saved.getTrainingDurationMinutes()).isEqualTo(60);

        // Verify metrics
        verify(trainingMetrics).startTimer();
        verify(trainingMetrics).stopTimer(timerSample);
        verify(trainingMetrics).incrementCreated();

        // Verify notification
        verify(workloadMessageProducer).sendNotification(
                eq(trainer),
                eq(LocalDate.of(2024, 5, 15)),
                eq(60),
                eq(TrainerWorkloadRequest.ActionType.ADD));
    }

    @Test
    @DisplayName("createTraining: throws ValidationException when validation fails")
    @SuppressWarnings("unchecked")
    void createTraining_validationFails_throwsException() {
        when(traineeService.getByUsername("jane.doe")).thenReturn(trainee);
        when(trainerService.getByUsername("john.trainer")).thenReturn(trainer);
        when(trainingMetrics.startTimer()).thenReturn(timerSample);

        ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Training name is required");
        when(validator.validate(any(Training.class))).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> trainingService.createTraining(addRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Training validation failed")
                .hasMessageContaining("Training name is required");

        verify(trainingRepository, never()).save(any());
        verify(workloadMessageProducer, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("createTraining: propagates exception if user not authenticated")
    void createTraining_unauthenticated_throws() {
        when(trainingMetrics.startTimer()).thenReturn(timerSample);
        doThrow(new RuntimeException("Not authenticated"))
                .when(userService).isAuthenticated("jane.doe");

        assertThatThrownBy(() -> trainingService.createTraining(addRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not authenticated");

        verify(trainingRepository, never()).save(any());
        verify(workloadMessageProducer, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getTraineeTrainingsByCriteria: returns all when no filters")
    void getTraineeTrainingsByCriteria_noFilters() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS);
        Training t2 = buildTraining(trainer, trainee, TrainingTypeName.YOGA);

        when(trainingRepository.findTrainingsWithAllUsers(
                eq("jane.doe"), isNull(), any(), any()))
                .thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                "jane.doe", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                null, null);

        assertThat(result).hasSize(2);
        verify(userService).isAuthenticated("jane.doe");
    }

    @Test
    @DisplayName("getTraineeTrainingsByCriteria: filters by trainerName")
    void getTraineeTrainingsByCriteria_filtersByTrainerName() {
        Trainer otherTrainer = buildTrainer("alice", "Alice", "Smith");
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS); // John Trainer
        Training t2 = buildTraining(otherTrainer, trainee, TrainingTypeName.YOGA); // Alice Smith

        when(trainingRepository.findTrainingsWithAllUsers(
                eq("jane.doe"), isNull(), any(), any()))
                .thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                "jane.doe", null, null, "alice", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainer().getUser().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("getTraineeTrainingsByCriteria: filters by trainingType")
    void getTraineeTrainingsByCriteria_filtersByType() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS);
        Training t2 = buildTraining(trainer, trainee, TrainingTypeName.YOGA);

        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                "jane.doe", null, null, null, TrainingTypeName.YOGA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainingType().getTrainingTypeName())
                .isEqualTo(TrainingTypeName.YOGA);
    }

    @Test
    @DisplayName("getTraineeTrainingsByCriteria: blank trainerName is ignored")
    void getTraineeTrainingsByCriteria_blankTrainerNameIgnored() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS);
        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                "jane.doe", null, null, "   ", null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getTraineeTrainingsByCriteria: returns empty list")
    void getTraineeTrainingsByCriteria_emptyResult() {
        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                "jane.doe", null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTrainerTrainingsByCriteria: returns all when no filters")
    void getTrainerTrainingsByCriteria_noFilters() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS);
        when(trainingRepository.findTrainingsWithAllUsers(
                isNull(), eq("john.trainer"), any(), any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                "john.trainer", LocalDate.now(), LocalDate.now(), null);

        assertThat(result).hasSize(1);
        verify(userService).isAuthenticated("john.trainer");
    }

    @Test
    @DisplayName("getTrainerTrainingsByCriteria: filters by traineeName")
    void getTrainerTrainingsByCriteria_filtersByTraineeName() {
        Trainee other = buildTrainee("bob", "Bob", "Brown");
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS); // Jane Doe
        Training t2 = buildTraining(trainer, other, TrainingTypeName.FITNESS);   // Bob Brown

        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                "john.trainer", null, null, "bob");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainee().getUser().getFirstName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("getTrainerTrainingsByCriteria: blank traineeName is ignored")
    void getTrainerTrainingsByCriteria_blankTraineeNameIgnored() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS);
        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                "john.trainer", null, null, "");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getTrainerTrainingsByCriteria: filter by lastName match")
    void getTrainerTrainingsByCriteria_matchLastName() {
        Training t1 = buildTraining(trainer, trainee, TrainingTypeName.FITNESS); // Jane Doe
        when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                .thenReturn(List.of(t1));

        List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                "john.trainer", null, null, "Doe");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllTrainingTypes: returns all types")
    void getAllTrainingTypes_returnsList() {
        TrainingType yoga = new TrainingType();
        yoga.setTrainingTypeName(TrainingTypeName.YOGA);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(trainingType, yoga));

        List<TrainingType> result = trainingService.getAllTrainingTypes();

        assertThat(result).hasSize(2);
        verify(trainingTypeRepository).findAll();
    }

    private Training buildTraining(Trainer trainer, Trainee trainee, TrainingTypeName typeName) {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName(typeName);
        return Training.builder()
                .trainer(trainer)
                .trainee(trainee)
                .trainingType(type)
                .trainingName("Training-" + typeName)
                .trainingDate(LocalDate.now())
                .trainingDurationMinutes(60)
                .build();
    }

    private Trainer buildTrainer(String username, String firstName, String lastName) {
        User u = new User();
        u.setUsername(username);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setIsActive(true);
        Trainer t = new Trainer();
        t.setUser(u);
        t.setSpecialization(trainingType);
        return t;
    }

    private Trainee buildTrainee(String username, String firstName, String lastName) {
        User u = new User();
        u.setUsername(username);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setIsActive(true);
        Trainee t = new Trainee();
        t.setUser(u);
        return t;
    }
}