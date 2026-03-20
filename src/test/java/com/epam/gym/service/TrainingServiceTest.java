package com.epam.gym.service;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private TrainingService trainingService;

    @Captor
    private ArgumentCaptor<Training> trainingCaptor;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType trainingType;
    private Training testTraining;

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String TRAINING_NAME = "Morning Workout";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 15);
    private static final Integer TRAINING_DURATION = 60;

    @BeforeEach
    void setUp() {
        User traineeUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(TRAINEE_USERNAME)
                .password("password123")
                .isActive(true)
                .build();

        User trainerUser = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .username(TRAINER_USERNAME)
                .password("trainerPass")
                .isActive(true)
                .build();

        trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        testTrainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        testTraining = Training.builder()
                .id(1L)
                .trainee(testTrainee)
                .trainer(testTrainer)
                .trainingName(TRAINING_NAME)
                .trainingType(trainingType)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .build();
    }

    // ==================== CREATE TRAINING TESTS ====================

    @Nested
    @DisplayName("createTraining Tests")
    class CreateTrainingTests {

        @Test
        @DisplayName("Should create training successfully")
        void createTraining_WithValidRequest_CreatesTraining() {
            // Arrange
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(testTrainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
                Training t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            // Act
            trainingService.createTraining(request);

            // Assert
            verify(userService).isAuthenticated(TRAINEE_USERNAME);
            verify(traineeService).getByUsername(TRAINEE_USERNAME);
            verify(trainerService).getByUsername(TRAINER_USERNAME);
            verify(validator).validate(any(Training.class));
            verify(trainingRepository).save(trainingCaptor.capture());

            Training capturedTraining = trainingCaptor.getValue();
            assertThat(capturedTraining.getTrainee()).isEqualTo(testTrainee);
            assertThat(capturedTraining.getTrainer()).isEqualTo(testTrainer);
            assertThat(capturedTraining.getTrainingName()).isEqualTo(TRAINING_NAME);
            assertThat(capturedTraining.getTrainingDate()).isEqualTo(TRAINING_DATE);
            assertThat(capturedTraining.getTrainingDurationMinutes()).isEqualTo(TRAINING_DURATION);
            assertThat(capturedTraining.getTrainingType()).isEqualTo(trainingType);
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createTraining_ValidationFails_ThrowsException() {
            // Arrange
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(testTrainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Training name is required");
            Set<ConstraintViolation<Training>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Training.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> trainingService.createTraining(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed")
                    .hasMessageContaining("Training name is required");

            verify(trainingRepository, never()).save(any(Training.class));
        }

        @Test
        @DisplayName("Should use trainer's specialization as training type")
        void createTraining_UsesTrainerSpecialization_AsTrainingType() {
            // Arrange
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeService.getByUsername(TRAINEE_USERNAME)).thenReturn(testTrainee);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            trainingService.createTraining(request);

            // Assert
            verify(trainingRepository).save(trainingCaptor.capture());
            Training capturedTraining = trainingCaptor.getValue();
            assertThat(capturedTraining.getTrainingType()).isEqualTo(testTrainer.getSpecialization());
        }
    }

    // ==================== GET TRAINEE TRAININGS BY CRITERIA TESTS ====================

    @Nested
    @DisplayName("getTraineeTrainingsByCriteria Tests")
    class GetTraineeTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should return trainings without filters")
        void getTraineeTrainings_WithoutFilters_ReturnsAllTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(testTraining);
            verify(userService).isAuthenticated(TRAINEE_USERNAME);
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTraineeTrainings_WithDateRange_ReturnsFilteredTrainings() {
            // Arrange
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 12, 31);
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), eq(fromDate), eq(toDate)))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, fromDate, toDate, null, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    TRAINEE_USERNAME, null, fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainer name")
        void getTraineeTrainings_WithTrainerName_ReturnsFilteredTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Jane", null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - case insensitive")
        void getTraineeTrainings_WithTrainerNameLowerCase_ReturnsFilteredTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "jane", null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer last name")
        void getTraineeTrainings_WithTrainerLastName_ReturnsFilteredTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "Smith", null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainer name - no match")
        void getTraineeTrainings_WithNonMatchingTrainerName_ReturnsEmpty() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "NonExistent", null);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter by training type")
        void getTraineeTrainings_WithTrainingType_ReturnsFilteredTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, TrainingTypeName.FITNESS);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by training type - no match")
        void getTraineeTrainings_WithNonMatchingTrainingType_ReturnsEmpty() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, TrainingTypeName.YOGA);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTraineeTrainings_NoTrainings_ReturnsEmptyList() {
            // Arrange
            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(new ArrayList<>());

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, null, null);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainer filter when blank")
        void getTraineeTrainings_WithBlankTrainerName_SkipsFilter() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq(TRAINEE_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    TRAINEE_USERNAME, null, null, "   ", null);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    // ==================== GET TRAINER TRAININGS BY CRITERIA TESTS ====================

    @Nested
    @DisplayName("getTrainerTrainingsByCriteria Tests")
    class GetTrainerTrainingsByCriteriaTests {

        @Test
        @DisplayName("Should return trainings without filters")
        void getTrainerTrainings_WithoutFilters_ReturnsAllTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, null);

            // Assert
            assertThat(result).hasSize(1);
            verify(userService).isAuthenticated(TRAINER_USERNAME);
        }

        @Test
        @DisplayName("Should filter by date range")
        void getTrainerTrainings_WithDateRange_ReturnsFilteredTrainings() {
            // Arrange
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 12, 31);
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), eq(fromDate), eq(toDate)))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, fromDate, toDate, null);

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name")
        void getTrainerTrainings_WithTraineeName_ReturnsFilteredTrainings() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "John");

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by trainee name - no match")
        void getTrainerTrainings_WithNonMatchingTraineeName_ReturnsEmpty() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "NonExistent");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should skip trainee filter when blank")
        void getTrainerTrainings_WithBlankTraineeName_SkipsFilter() {
            // Arrange
            List<Training> trainings = new ArrayList<>(List.of(testTraining));

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq(TRAINER_USERNAME), isNull(), isNull()))
                    .thenReturn(trainings);

            // Act
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, "   ");

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    // ==================== GET ALL TRAINING TYPES TESTS ====================

    @Nested
    @DisplayName("getAllTrainingTypes Tests")
    class GetAllTrainingTypesTests {

        @Test
        @DisplayName("Should return all training types")
        void getAllTrainingTypes_ReturnsAllTypes() {
            // Arrange
            TrainingType type1 = new TrainingType(1L, TrainingTypeName.FITNESS);
            TrainingType type2 = new TrainingType(2L, TrainingTypeName.YOGA);
            TrainingType type3 = new TrainingType(3L, TrainingTypeName.CARDIO);

            List<TrainingType> trainingTypes = List.of(type1, type2, type3);

            when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

            // Act
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(type1, type2, type3);
            verify(trainingTypeRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no training types")
        void getAllTrainingTypes_NoTypes_ReturnsEmptyList() {
            // Arrange
            when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Assert
            assertThat(result).isEmpty();
        }
    }
}