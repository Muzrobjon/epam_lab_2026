package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingService Tests")
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

    @Captor
    private ArgumentCaptor<Training> trainingCaptor;

    private User trainerUser;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType fitnessType;
    private TrainingType yogaType;
    private Training testTraining;
    private Timer.Sample mockTimerSample;

    private TrainingType createTrainingType(Long id, TrainingTypeName name) {
        TrainingType type = new TrainingType();
        ReflectionTestUtils.setField(type, "id", id);
        ReflectionTestUtils.setField(type, "trainingTypeName", name);
        return type;
    }

    @BeforeEach
    void setUp() {
        User traineeUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("pass123")
                .isActive(true)
                .build();

        trainerUser = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Trainer")
                .username("Mike.Trainer")
                .password("pass456")
                .isActive(true)
                .build();

        fitnessType = createTrainingType(1L, TrainingTypeName.FITNESS);
        yogaType = createTrainingType(2L, TrainingTypeName.YOGA);

        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .trainers(new ArrayList<>())
                .build();

        testTrainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(fitnessType)
                .trainees(new ArrayList<>())
                .build();

        testTraining = Training.builder()
                .id(1L)
                .trainee(testTrainee)
                .trainer(testTrainer)
                .trainingName("Morning Fitness")
                .trainingType(fitnessType)
                .trainingDate(LocalDate.of(2024, 3, 15))
                .trainingDurationMinutes(60)
                .build();

        mockTimerSample = mock(Timer.Sample.class);
    }

    @Nested
    @DisplayName("createTraining Tests")
    class CreateTrainingTests {

        @Test
        @DisplayName("Should create training successfully")
        void createTraining_Success() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Morning Fitness")
                    .trainingDate(LocalDate.of(2024, 3, 15))
                    .trainingDuration(60)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(userService).isAuthenticated("John.Doe");
            verify(traineeService).getByUsername("John.Doe");
            verify(trainerService).getByUsername("Mike.Trainer");
            verify(validator).validate(any(Training.class));
            verify(trainingRepository).save(trainingCaptor.capture());
            verify(trainingMetrics).startTimer();
            verify(trainingMetrics).stopTimer(mockTimerSample);
            verify(trainingMetrics).incrementCreated();

            Training captured = trainingCaptor.getValue();
            assertThat(captured.getTrainee()).isEqualTo(testTrainee);
            assertThat(captured.getTrainer()).isEqualTo(testTrainer);
            assertThat(captured.getTrainingName()).isEqualTo("Morning Fitness");
            assertThat(captured.getTrainingType()).isEqualTo(fitnessType);
            assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 15));
            assertThat(captured.getTrainingDurationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should use trainer's specialization as training type")
        void createTraining_UsesTrainerSpecialization() {
            // Given
            Trainer yogaTrainer = Trainer.builder()
                    .id(2L)
                    .user(trainerUser)
                    .specialization(yogaType)
                    .build();

            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Yoga Session")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(90)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(yogaTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            Training captured = trainingCaptor.getValue();
            assertThat(captured.getTrainingType()).isEqualTo(yogaType);
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createTraining_ValidationFails() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(0)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Training name is required");

            Set<ConstraintViolation<Training>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Training.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed")
                    .hasMessageContaining("Training name is required");

            verify(trainingRepository, never()).save(any());
            verify(trainingMetrics, never()).stopTimer(any());
            verify(trainingMetrics, never()).incrementCreated();
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple errors")
        void createTraining_MultipleValidationErrors() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("")
                    .trainingDate(null)
                    .trainingDuration(-1)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation1 = mock(ConstraintViolation.class);
            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation2 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Name required");
            when(violation2.getMessage()).thenReturn("Date required");

            Set<ConstraintViolation<Training>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            when(validator.validate(any(Training.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should authenticate user before creating training")
        void createTraining_AuthenticatesFirst() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Test Training")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }
    }

    @Nested
    @DisplayName("getTraineeTrainingsByCriteria Tests")
    class GetTraineeTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should return all trainings for trainee")
        void getTraineeTrainings_Success() {
            // Given
            List<Training> trainings = List.of(testTraining);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(testTraining);
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTraineeTrainings_FilterByDateRange() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 3, 1);
            LocalDate toDate = LocalDate.of(2024, 3, 31);
            List<Training> trainings = List.of(testTraining);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), eq(fromDate), eq(toDate)))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", fromDate, toDate, null, null);

            // Then
            assertThat(result).hasSize(1);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    "John.Doe", null, fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainer name - first name match")
        void getTraineeTrainings_FilterByTrainerFirstName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "Mike", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - last name match")
        void getTraineeTrainings_FilterByTrainerLastName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "Trainer", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - case insensitive")
        void getTraineeTrainings_FilterByTrainerName_CaseInsensitive() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "MIKE", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - no match")
        void getTraineeTrainings_FilterByTrainerName_NoMatch() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "Unknown", null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter by training type")
        void getTraineeTrainings_FilterByTrainingType() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, TrainingTypeName.FITNESS);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by training type - no match")
        void getTraineeTrainings_FilterByTrainingType_NoMatch() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, TrainingTypeName.YOGA);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should combine multiple filters")
        void getTraineeTrainings_CombineFilters() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), any(), any()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe",
                    LocalDate.of(2024, 3, 1),
                    LocalDate.of(2024, 3, 31),
                    "Mike",
                    TrainingTypeName.FITNESS);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no trainings")
        void getTraineeTrainings_EmptyList() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainer name filter when blank")
        void getTraineeTrainings_BlankTrainerName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "   ", null);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getTrainerTrainingsByCriteria Tests")
    class GetTrainerTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should return all trainings for trainer")
        void getTrainerTrainings_Success() {
            // Given
            List<Training> trainings = List.of(testTraining);

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(testTraining);
            verify(userService).isAuthenticated("Mike.Trainer");
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTrainerTrainings_FilterByDateRange() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 3, 1);
            LocalDate toDate = LocalDate.of(2024, 3, 31);
            List<Training> trainings = List.of(testTraining);

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), eq(fromDate), eq(toDate)))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", fromDate, toDate, null);

            // Then
            assertThat(result).hasSize(1);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    null, "Mike.Trainer", fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainee name - first name match")
        void getTrainerTrainings_FilterByTraineeFirstName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, "John");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - last name match")
        void getTrainerTrainings_FilterByTraineeLastName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, "Doe");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - case insensitive")
        void getTrainerTrainings_FilterByTraineeName_CaseInsensitive() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, "JOHN");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - no match")
        void getTrainerTrainings_FilterByTraineeName_NoMatch() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, "Unknown");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no trainings")
        void getTrainerTrainings_EmptyList() {
            // Given
            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainee name filter when blank")
        void getTrainerTrainings_BlankTraineeName() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, "   ");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should combine date and trainee name filters")
        void getTrainerTrainings_CombineFilters() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("Mike.Trainer"), any(), any()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer",
                    LocalDate.of(2024, 3, 1),
                    LocalDate.of(2024, 3, 31),
                    "John");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAllTrainingTypes Tests")
    class GetAllTrainingTypesTests {

        @Test
        @DisplayName("Should return all training types")
        void getAllTrainingTypes_Success() {
            // Given
            List<TrainingType> types = List.of(fitnessType, yogaType);
            when(trainingTypeRepository.findAll()).thenReturn(types);

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(fitnessType, yogaType);
            verify(trainingTypeRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no training types")
        void getAllTrainingTypes_EmptyList() {
            // Given
            when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return all available training types")
        void getAllTrainingTypes_AllTypes() {
            // Given
            List<TrainingType> types = new ArrayList<>();
            for (TrainingTypeName name : TrainingTypeName.values()) {
                types.add(createTrainingType((long) name.ordinal(), name));
            }
            when(trainingTypeRepository.findAll()).thenReturn(types);

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).hasSize(TrainingTypeName.values().length);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle training with minimum duration")
        void createTraining_MinimumDuration() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Quick Session")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(1)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingDurationMinutes()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle training with long duration")
        void createTraining_LongDuration() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Marathon Session")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(480) // 8 hours
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingDurationMinutes()).isEqualTo(480);
        }

        @Test
        @DisplayName("Should handle future training date")
        void createTraining_FutureDate() {
            // Given
            LocalDate futureDate = LocalDate.now().plusMonths(3);
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Future Session")
                    .trainingDate(futureDate)
                    .trainingDuration(60)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingDate()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("Should handle multiple trainings for same trainee")
        void getTraineeTrainings_MultipleTrainings() {
            // Given
            Training training2 = Training.builder()
                    .id(2L)
                    .trainee(testTrainee)
                    .trainer(testTrainer)
                    .trainingName("Evening Fitness")
                    .trainingType(fitnessType)
                    .trainingDate(LocalDate.of(2024, 3, 16))
                    .trainingDurationMinutes(45)
                    .build();

            List<Training> trainings = List.of(testTraining, training2);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, null);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should filter trainings with partial trainer name match")
        void getTraineeTrainings_PartialTrainerNameMatch() {
            // Given
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("John.Doe"), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // When - partial match "ik" should match "Mike"
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, "ik", null);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle training name with special characters")
        void createTraining_SpecialCharactersInName() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("HIIT Training (Advanced) - Level 3!")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingName())
                    .isEqualTo("HIIT Training (Advanced) - Level 3!");
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("createTraining should authenticate trainee")
        void createTraining_AuthenticatesTrainee() {
            // Given
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("John.Doe")
                    .trainerUsername("Mike.Trainer")
                    .trainingName("Test")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            when(trainingMetrics.startTimer()).thenReturn(mockTimerSample);
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeService.getByUsername("John.Doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("Mike.Trainer")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

            // When
            trainingService.createTraining(request);

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("getTraineeTrainingsByCriteria should authenticate user")
        void getTraineeTrainings_AuthenticatesUser() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            trainingService.getTraineeTrainingsByCriteria(
                    "John.Doe", null, null, null, null);

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("getTrainerTrainingsByCriteria should authenticate user")
        void getTrainerTrainings_AuthenticatesUser() {
            // Given
            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainingRepository.findTrainingsWithAllUsers(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            trainingService.getTrainerTrainingsByCriteria(
                    "Mike.Trainer", null, null, null);

            // Then
            verify(userService).isAuthenticated("Mike.Trainer");
        }
    }
}