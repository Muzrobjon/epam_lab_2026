package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.metrics.TrainingMetrics;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private Validator validator;

    @Mock
    private UserService userService;

    @Mock
    private TrainingMetrics trainingMetrics;

    @InjectMocks
    private TrainingService trainingService;

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Mike.Tyson";

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;

    @BeforeEach
    void setUp() {
        User traineeUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(TRAINEE_USERNAME)
                .isActive(true)
                .build();

        User trainerUser = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Tyson")
                .username(TRAINER_USERNAME)
                .isActive(true)
                .build();

        trainingType = TrainingType.builder()
                .id(1L)
                .trainingTypeName(TrainingTypeName.FITNESS)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .trainees(List.of())
                .build();

        training = Training.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Workout")
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2025, 6, 15))
                .trainingDurationMinutes(60)
                .build();
    }

    // ==================== CREATE TRAINING TESTS ====================

    @Nested
    @DisplayName("Create Training Tests")
    class CreateTrainingTests {

        private AddTrainingRequest request;

        @BeforeEach
        void setUp() {
            request = new AddTrainingRequest();
            request.setTraineeUsername(TRAINEE_USERNAME);
            request.setTrainerUsername(TRAINER_USERNAME);
            request.setTrainingName("Morning Workout");
            request.setTrainingDate(LocalDate.of(2025, 6, 15));
            request.setTrainingDuration(60);
        }

        @Test
        @DisplayName("Should create training successfully")
        void createTraining_Success() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);

            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(training);

            // When
            trainingService.createTraining(request);

            // Then
            verify(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            verify(traineeService).getByUsername(TRAINEE_USERNAME);
            verify(trainerService).getByUsername(TRAINER_USERNAME);
            verify(validator).validate(any(Training.class));
            verify(trainingRepository).save(any(Training.class));
            verify(trainingMetrics).startTimer();
            verify(trainingMetrics).stopTimer(timerSample);
            verify(trainingMetrics).incrementCreated();
        }

        @Test
        @DisplayName("Should use trainer's specialization as training type")
        void createTraining_UsesTrainerSpecialization() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
                Training saved = invocation.getArgument(0);
                assertThat(saved.getTrainingType()).isEqualTo(trainingType);
                assertThat(saved.getTrainingType().getTrainingTypeName())
                        .isEqualTo(TrainingTypeName.FITNESS);
                return saved;
            });

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(any(Training.class));
        }

        @Test
        @DisplayName("Should set all fields from request")
        void createTraining_SetsAllFields() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
                Training saved = invocation.getArgument(0);
                assertThat(saved.getTrainee()).isEqualTo(trainee);
                assertThat(saved.getTrainer()).isEqualTo(trainer);
                assertThat(saved.getTrainingName()).isEqualTo("Morning Workout");
                assertThat(saved.getTrainingDate()).isEqualTo(LocalDate.of(2025, 6, 15));
                assertThat(saved.getTrainingDurationMinutes()).isEqualTo(60);
                return saved;
            });

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(any(Training.class));
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void createTraining_NotOwner_ThrowsAuthException() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(TRAINEE_USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeService, never()).getByUsername(anyString());
            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void createTraining_TraineeNotFound_ThrowsNotFoundException() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME))
                    .thenThrow(new NotFoundException("Trainee not found: " + TRAINEE_USERNAME));

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void createTraining_TrainerNotFound_ThrowsNotFoundException() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME))
                    .thenThrow(new NotFoundException("Trainer not found: " + TRAINER_USERNAME));

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createTraining_ValidationFails_ThrowsValidationException() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Training name is required");
            when(validator.validate(any(Training.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed")
                    .hasMessageContaining("Training name is required");

            verify(trainingRepository, never()).save(any());
            verify(trainingMetrics, never()).incrementCreated();
        }

        @Test
        @DisplayName("Should record metrics on successful creation")
        void createTraining_RecordsMetrics() {
            // Given
            Timer.Sample timerSample = mock(Timer.Sample.class);
            when(trainingMetrics.startTimer()).thenReturn(timerSample);
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(trainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(trainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(training);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingMetrics).startTimer();
            verify(trainingMetrics).stopTimer(timerSample);
            verify(trainingMetrics).incrementCreated();
        }
    }

    // ==================== GET TRAINEE TRAININGS BY CRITERIA TESTS ====================

    @Nested
    @DisplayName("Get Trainee Trainings By Criteria Tests")
    class GetTraineeTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should get trainee trainings without filters")
        void getTraineeTrainings_NoFilters_Success() {
            // Given
            List<Training> trainings = List.of(training);

            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    TRAINEE_USERNAME, null, null, null))
                    .thenReturn(new ArrayList<>(trainings));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Workout");

            verify(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    TRAINEE_USERNAME, null, null, null);
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTraineeTrainings_WithDateRange_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.of(2025, 12, 31);

            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    TRAINEE_USERNAME, null, fromDate, toDate))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, fromDate, toDate, null, null);

            // Then
            assertThat(result).hasSize(1);

            verify(trainingRepository).findTrainingsWithAllUsers(
                    TRAINEE_USERNAME, null, fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainer name - first name match")
        void getTraineeTrainings_FilterByTrainerFirstName_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Mike", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - last name match")
        void getTraineeTrainings_FilterByTrainerLastName_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Tyson", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - case insensitive")
        void getTraineeTrainings_FilterByTrainerName_CaseInsensitive() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "mike", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - no match")
        void getTraineeTrainings_FilterByTrainerName_NoMatch() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "NonExistent", null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainer name filter when blank")
        void getTraineeTrainings_BlankTrainerName_SkipsFilter() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "   ", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by training type")
        void getTraineeTrainings_FilterByTrainingType_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, TrainingTypeName.FITNESS);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by training type - no match")
        void getTraineeTrainings_FilterByTrainingType_NoMatch() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, TrainingTypeName.YOGA);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should apply both trainer name and training type filters")
        void getTraineeTrainings_BothFilters_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Mike", TrainingTypeName.FITNESS);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when both filters don't match")
        void getTraineeTrainings_BothFiltersDontMatch_ReturnsEmpty() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Mike", TrainingTypeName.YOGA);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTraineeTrainings_NoTrainings_ReturnsEmptyList() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>());

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void getTraineeTrainings_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(TRAINEE_USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, null))
                    .isInstanceOf(AuthenticationException.class);

            verify(trainingRepository, never()).findTrainingsWithAllUsers(
                    anyString(), any(), any(), any());
        }
    }

    // ==================== GET TRAINER TRAININGS BY CRITERIA TESTS ====================

    @Nested
    @DisplayName("Get Trainer Trainings By Criteria Tests")
    class GetTrainerTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should get trainer trainings without filters")
        void getTrainerTrainings_NoFilters_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, TRAINER_USERNAME, null, null))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, null);

            // Then
            assertThat(result).hasSize(1);

            verify(userService).verifyResourceOwnership(TRAINER_USERNAME);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    null, TRAINER_USERNAME, null, null);
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTrainerTrainings_WithDateRange_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.of(2025, 12, 31);

            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, TRAINER_USERNAME, fromDate, toDate))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, fromDate, toDate, null);

            // Then
            assertThat(result).hasSize(1);

            verify(trainingRepository).findTrainingsWithAllUsers(
                    null, TRAINER_USERNAME, fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainee name - first name match")
        void getTrainerTrainings_FilterByTraineeFirstName_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "John");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - last name match")
        void getTrainerTrainings_FilterByTraineeLastName_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "Doe");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - case insensitive")
        void getTrainerTrainings_FilterByTraineeName_CaseInsensitive() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "john");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - no match")
        void getTrainerTrainings_FilterByTraineeName_NoMatch() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "NonExistent");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainee name filter when blank")
        void getTrainerTrainings_BlankTraineeName_SkipsFilter() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>(List.of(training)));

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "   ");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTrainerTrainings_NoTrainings_ReturnsEmptyList() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(new ArrayList<>());

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void getTrainerTrainings_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(TRAINER_USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, null))
                    .isInstanceOf(AuthenticationException.class);

            verify(trainingRepository, never()).findTrainingsWithAllUsers(
                    any(), anyString(), any(), any());
        }
    }

    // ==================== GET ALL TRAINING TYPES TESTS ====================

    @Nested
    @DisplayName("Get All Training Types Tests")
    class GetAllTrainingTypesTests {

        @Test
        @DisplayName("Should get all training types successfully")
        void getAllTrainingTypes_Success() {
            // Given
            List<TrainingType> trainingTypes = List.of(
                    TrainingType.builder().id(1L).trainingTypeName(TrainingTypeName.FITNESS).build(),
                    TrainingType.builder().id(2L).trainingTypeName(TrainingTypeName.YOGA).build(),
                    TrainingType.builder().id(3L).trainingTypeName(TrainingTypeName.ZUMBA).build()
            );

            when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(result.get(1).getTrainingTypeName()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(result.get(2).getTrainingTypeName()).isEqualTo(TrainingTypeName.ZUMBA);

            verify(trainingTypeRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no training types")
        void getAllTrainingTypes_NoTypes_ReturnsEmptyList() {
            // Given
            when(trainingTypeRepository.findAll()).thenReturn(List.of());

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should delegate to repository")
        void getAllTrainingTypes_DelegatesToRepository() {
            // Given
            when(trainingTypeRepository.findAll()).thenReturn(List.of());

            // When
            trainingService.getAllTrainingTypes();

            // Then
            verify(trainingTypeRepository, times(1)).findAll();
            verifyNoInteractions(trainingRepository);
            verifyNoInteractions(userService);
        }
    }
}