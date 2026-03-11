package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private Validator validator;

    @Captor
    private ArgumentCaptor<Training> trainingCaptor;

    private TrainingService trainingService;

    // Test data
    private User traineeUser;
    private User trainerUser;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService(
                trainingRepository,
                traineeService,
                trainerService,
                trainingTypeRepository,
                validator
        );

        // Setup common test data
        traineeUser = User.builder()
                .id(1L)
                .username("john.doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        trainerUser = User.builder()
                .id(2L)
                .username("jane.trainer")
                .password("trainerPass")
                .firstName("Jane")
                .lastName("Trainer")
                .isActive(true)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .build();

        trainingType = new TrainingType(1L, TrainingTypeName.CARDIO);

        trainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .build();
    }

    @Nested
    @DisplayName("createTraining Tests")
    class CreateTrainingTests {

        @Test
        @DisplayName("Should create training successfully")
        void shouldCreateTrainingSuccessfully() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            String trainingName = "Morning Cardio";
            TrainingTypeName trainingTypeName = TrainingTypeName.CARDIO;
            LocalDate trainingDate = LocalDate.now().plusDays(1);
            Integer duration = 60;

            Training savedTraining = Training.builder()
                    .id(1L)
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingName(trainingName)
                    .trainingType(trainingType)
                    .trainingDate(trainingDate)
                    .trainingDurationMinutes(duration)
                    .build();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
            when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
            when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName))
                    .thenReturn(Optional.of(trainingType));
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);

            // When
            Training result = trainingService.createTraining(
                    traineeUsername, traineePassword,
                    trainerUsername, trainerPassword,
                    trainingName, trainingTypeName,
                    trainingDate, duration
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTrainingName()).isEqualTo(trainingName);

            verify(traineeService).authenticateUser(traineeUsername, traineePassword);
            verify(trainerService).authenticateUser(trainerUsername, trainerPassword);
            verify(trainingRepository).save(trainingCaptor.capture());

            Training captured = trainingCaptor.getValue();
            assertThat(captured.getTrainee()).isEqualTo(trainee);
            assertThat(captured.getTrainer()).isEqualTo(trainer);
            assertThat(captured.getTrainingType()).isEqualTo(trainingType);
            assertThat(captured.getTrainingDurationMinutes()).isEqualTo(duration);
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void shouldThrowNotFoundExceptionWhenTrainingTypeNotFound() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            TrainingTypeName trainingTypeName = TrainingTypeName.YOGA;

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
            when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
            when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(
                    traineeUsername, traineePassword,
                    trainerUsername, trainerPassword,
                    "Training", trainingTypeName,
                    LocalDate.now(), 60
            ))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found: " + trainingTypeName);

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when training is invalid")
        void shouldThrowValidationExceptionWhenTrainingInvalid() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            TrainingTypeName trainingTypeName = TrainingTypeName.CARDIO;

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Training name is required");

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(traineeService.selectByUsername(traineeUsername)).thenReturn(trainee);
            when(trainerService.selectByUsername(trainerUsername)).thenReturn(trainer);
            when(trainingTypeRepository.findByTrainingTypeName(trainingTypeName))
                    .thenReturn(Optional.of(trainingType));
            when(validator.validate(any(Training.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(
                    traineeUsername, traineePassword,
                    trainerUsername, trainerPassword,
                    null, trainingTypeName,
                    LocalDate.now(), 60
            ))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed")
                    .hasMessageContaining("Training name is required");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when trainee authentication fails")
        void shouldThrowExceptionWhenTraineeAuthenticationFails() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "wrongPassword";

            doThrow(new RuntimeException("Authentication failed"))
                    .when(traineeService).authenticateUser(traineeUsername, traineePassword);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(
                    traineeUsername, traineePassword,
                    "trainer", "pass",
                    "Training", TrainingTypeName.CARDIO,
                    LocalDate.now(), 60
            ))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Authentication failed");

            verify(trainerService, never()).authenticateUser(anyString(), anyString());
            verify(trainingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTraineeTrainingsByCriteria Tests")
    class GetTraineeTrainingsByCriteriaTests {

        private List<Training> createSampleTrainings() {
            User trainer1User = User.builder()
                    .firstName("Mike")
                    .lastName("Johnson")
                    .build();
            Trainer trainer1 = Trainer.builder()
                    .id(1L)
                    .user(trainer1User)
                    .build();

            User trainer2User = User.builder()
                    .firstName("Sarah")
                    .lastName("Smith")
                    .build();
            Trainer trainer2 = Trainer.builder()
                    .id(2L)
                    .user(trainer2User)
                    .build();

            TrainingType cardio = new TrainingType(1L, TrainingTypeName.CARDIO);
            TrainingType yoga = new TrainingType(2L, TrainingTypeName.YOGA);

            return List.of(
                    Training.builder()
                            .id(1L)
                            .trainer(trainer1)
                            .trainingType(cardio)
                            .trainingName("Cardio Session")
                            .build(),
                    Training.builder()
                            .id(2L)
                            .trainer(trainer2)
                            .trainingType(yoga)
                            .trainingName("Yoga Session")
                            .build(),
                    Training.builder()
                            .id(3L)
                            .trainer(trainer1)
                            .trainingType(yoga)
                            .trainingName("Morning Yoga")
                            .build()
            );
        }

        @Test
        @DisplayName("Should return all trainings without filters")
        void shouldReturnAllTrainingsWithoutFilters() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, null, null
            );

            // Then
            assertThat(result).hasSize(3);
            verify(traineeService).authenticateUser(traineeUsername, traineePassword);
        }

        @Test
        @DisplayName("Should filter by trainer name")
        void shouldFilterByTrainerName() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, "Mike", null
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t ->
                    t.getTrainer().getUser().getFirstName().contains("Mike"));
        }

        @Test
        @DisplayName("Should filter by training type")
        void shouldFilterByTrainingType() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, null, TrainingTypeName.YOGA
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t ->
                    t.getTrainingType().getTrainingTypeName() == TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should filter by trainer name and training type")
        void shouldFilterByTrainerNameAndTrainingType() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, "Mike", TrainingTypeName.YOGA
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTrainingName()).isEqualTo("Morning Yoga");
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            LocalDate fromDate = LocalDate.now().minusDays(7);
            LocalDate toDate = LocalDate.now();
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, fromDate, toDate))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    fromDate, toDate, null, null
            );

            // Then
            assertThat(result).hasSize(3);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    traineeUsername, null, fromDate, toDate);
        }

        @Test
        @DisplayName("Should handle case-insensitive trainer name search")
        void shouldHandleCaseInsensitiveTrainerNameSearch() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, "MIKE", null
            );

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no matching trainings")
        void shouldReturnEmptyListWhenNoMatchingTrainings() {
            // Given
            String traineeUsername = "john.doe";
            String traineePassword = "password123";
            List<Training> trainings = createSampleTrainings();

            doNothing().when(traineeService).authenticateUser(traineeUsername, traineePassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, null, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword,
                    null, null, "NonExistent", null
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTrainerTrainingsByCriteria Tests")
    class GetTrainerTrainingsByCriteriaTests {

        private List<Training> createTrainerTrainings() {
            User trainee1User = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();
            Trainee trainee1 = Trainee.builder()
                    .id(1L)
                    .user(trainee1User)
                    .build();

            User trainee2User = User.builder()
                    .firstName("Alice")
                    .lastName("Wonder")
                    .build();
            Trainee trainee2 = Trainee.builder()
                    .id(2L)
                    .user(trainee2User)
                    .build();

            return List.of(
                    Training.builder()
                            .id(1L)
                            .trainee(trainee1)
                            .trainingName("Session 1")
                            .build(),
                    Training.builder()
                            .id(2L)
                            .trainee(trainee2)
                            .trainingName("Session 2")
                            .build(),
                    Training.builder()
                            .id(3L)
                            .trainee(trainee1)
                            .trainingName("Session 3")
                            .build()
            );
        }

        @Test
        @DisplayName("Should return all trainings without filters")
        void shouldReturnAllTrainingsWithoutFilters() {
            // Given
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            List<Training> trainings = createTrainerTrainings();

            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, trainerUsername, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    trainerUsername, trainerPassword,
                    null, null, null
            );

            // Then
            assertThat(result).hasSize(3);
            verify(trainerService).authenticateUser(trainerUsername, trainerPassword);
        }

        @Test
        @DisplayName("Should filter by trainee name")
        void shouldFilterByTraineeName() {
            // Given
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            List<Training> trainings = createTrainerTrainings();

            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, trainerUsername, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    trainerUsername, trainerPassword,
                    null, null, "John"
            );

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t ->
                    t.getTrainee().getUser().getFirstName().contains("John"));
        }

        @Test
        @DisplayName("Should filter by trainee last name")
        void shouldFilterByTraineeLastName() {
            // Given
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            List<Training> trainings = createTrainerTrainings();

            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, trainerUsername, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    trainerUsername, trainerPassword,
                    null, null, "Wonder"
            );

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTrainee().getUser().getLastName()).isEqualTo("Wonder");
        }

        @Test
        @DisplayName("Should handle blank trainee name")
        void shouldHandleBlankTraineeName() {
            // Given
            String trainerUsername = "jane.trainer";
            String trainerPassword = "trainerPass";
            List<Training> trainings = createTrainerTrainings();

            doNothing().when(trainerService).authenticateUser(trainerUsername, trainerPassword);
            when(trainingRepository.findTrainingsWithAllUsers(
                    null, trainerUsername, null, null))
                    .thenReturn(trainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    trainerUsername, trainerPassword,
                    null, null, "   "
            );

            // Then
            assertThat(result).hasSize(3); // No filtering applied
        }
    }

    @Nested
    @DisplayName("getTrainingsWithAllUsers Tests")
    class GetTrainingsWithAllUsersTests {

        @Test
        @DisplayName("Should return trainings with all parameters")
        void shouldReturnTrainingsWithAllParameters() {
            // Given
            String traineeUsername = "john.doe";
            String trainerUsername = "jane.trainer";
            LocalDate fromDate = LocalDate.now().minusDays(30);
            LocalDate toDate = LocalDate.now();

            List<Training> expectedTrainings = List.of(
                    Training.builder().id(1L).build(),
                    Training.builder().id(2L).build()
            );

            when(trainingRepository.findTrainingsWithAllUsers(
                    traineeUsername, trainerUsername, fromDate, toDate))
                    .thenReturn(expectedTrainings);

            // When
            List<Training> result = trainingService.getTrainingsWithAllUsers(
                    traineeUsername, trainerUsername, fromDate, toDate
            );

            // Then
            assertThat(result).hasSize(2);
            verify(trainingRepository).findTrainingsWithAllUsers(
                    traineeUsername, trainerUsername, fromDate, toDate);
        }

        @Test
        @DisplayName("Should return trainings with null parameters")
        void shouldReturnTrainingsWithNullParameters() {
            // Given
            List<Training> expectedTrainings = List.of(
                    Training.builder().id(1L).build()
            );

            when(trainingRepository.findTrainingsWithAllUsers(null, null, null, null))
                    .thenReturn(expectedTrainings);

            // When
            List<Training> result = trainingService.getTrainingsWithAllUsers(
                    null, null, null, null
            );

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void shouldReturnEmptyListWhenNoTrainingsFound() {
            // Given
            when(trainingRepository.findTrainingsWithAllUsers(
                    "nonexistent", null, null, null))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTrainingsWithAllUsers(
                    "nonexistent", null, null, null
            );

            // Then
            assertThat(result).isEmpty();
        }
    }
}