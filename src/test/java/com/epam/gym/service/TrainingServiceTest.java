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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @InjectMocks
    private TrainingService trainingService;

    @Captor
    private ArgumentCaptor<Training> trainingCaptor;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType cardioType;
    private TrainingType strengthType;

    @BeforeEach
    void setUp() {
        User traineeUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("traineePass")
                .isActive(true)
                .build();

        User trainerUser = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Johnson")
                .username("mike.johnson")
                .password("trainerPass")
                .isActive(true)
                .build();

        cardioType = new TrainingType(1L, TrainingTypeName.CARDIO);
        strengthType = new TrainingType(2L, TrainingTypeName.STRENGTH);

        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .build();

        testTrainer = Trainer.builder()
                .id(2L)
                .user(trainerUser)
                .specialization(cardioType)
                .build();
    }

    @Nested
    @DisplayName("createTraining Tests")
    class CreateTrainingTests {

        private AddTrainingRequest createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new AddTrainingRequest();
            createRequest.setTraineeUsername("john.doe");
            createRequest.setTraineePassword("traineePass");
            createRequest.setTrainerUsername("mike.johnson");
            createRequest.setTrainerPassword("trainerPass");
            createRequest.setTrainingName("Morning Cardio");
            createRequest.setTrainingDate(LocalDate.of(2024, 6, 15));
            createRequest.setTrainingDuration(60);
        }

        @Test
        @DisplayName("Should create training successfully")
        void shouldCreateTrainingSuccessfully() {
            // Given
            Training savedTraining = Training.builder()
                    .id(1L)
                    .trainee(testTrainee)
                    .trainer(testTrainer)
                    .trainingName("Morning Cardio")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingDurationMinutes(60)
                    .build();

            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);

            // When
            trainingService.createTraining(createRequest);

            // Then
            verify(traineeService).authenticate("john.doe", "traineePass");
            verify(trainerService).authenticate("mike.johnson", "trainerPass");
            verify(traineeService).getByUsername("john.doe");
            verify(trainerService).getByUsername("mike.johnson");
            verify(validator).validate(any(Training.class));
            verify(trainingRepository).save(trainingCaptor.capture());

            Training capturedTraining = trainingCaptor.getValue();
            assertThat(capturedTraining.getTrainee()).isEqualTo(testTrainee);
            assertThat(capturedTraining.getTrainer()).isEqualTo(testTrainer);
            assertThat(capturedTraining.getTrainingName()).isEqualTo("Morning Cardio");
            assertThat(capturedTraining.getTrainingType()).isEqualTo(cardioType);
            assertThat(capturedTraining.getTrainingDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(capturedTraining.getTrainingDurationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should use trainer's specialization as training type")
        void shouldUseTrainerSpecializationAsTrainingType() {
            // Given
            testTrainer.setSpecialization(strengthType);

            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            trainingService.createTraining(createRequest);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingType()).isEqualTo(strengthType);
        }

        @Test
        @DisplayName("Should throw exception when trainee authentication fails")
        void shouldThrowExceptionWhenTraineeAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid trainee credentials"))
                    .when(traineeService).authenticate("john.doe", "traineePass");

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid trainee credentials");

            verifyNoInteractions(trainerService);
            verifyNoInteractions(trainingRepository);
        }

        @Test
        @DisplayName("Should throw exception when trainer authentication fails")
        void shouldThrowExceptionWhenTrainerAuthenticationFails() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doThrow(new RuntimeException("Invalid trainer credentials"))
                    .when(trainerService).authenticate("mike.johnson", "trainerPass");

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid trainer credentials");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void shouldThrowValidationExceptionWhenValidationFails() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Training name is required");
            Set<ConstraintViolation<Training>> violations = new HashSet<>();
            violations.add(violation);

            when(validator.validate(any(Training.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(createRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed")
                    .hasMessageContaining("Training name is required");

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Error 1");

            @SuppressWarnings("unchecked")
            ConstraintViolation<Training> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("Error 2");

            Set<ConstraintViolation<Training>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);

            when(validator.validate(any(Training.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainingService.createTraining(createRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Training validation failed");

            verify(trainingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTraineeTrainingsByCriteria Tests")
    class GetTraineeTrainingsByCriteriaTests {

        private List<Training> sampleTrainings;

        @BeforeEach
        void setUp() {
            User trainer1User = User.builder()
                    .id(10L)
                    .firstName("Mike")
                    .lastName("Johnson")
                    .username("mike.johnson")
                    .build();

            User trainer2User = User.builder()
                    .id(11L)
                    .firstName("Sarah")
                    .lastName("Wilson")
                    .username("sarah.wilson")
                    .build();

            Trainer trainer1 = Trainer.builder()
                    .id(10L)
                    .user(trainer1User)
                    .specialization(cardioType)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(11L)
                    .user(trainer2User)
                    .specialization(strengthType)
                    .build();

            Training training1 = Training.builder()
                    .id(1L)
                    .trainee(testTrainee)
                    .trainer(trainer1)
                    .trainingName("Morning Cardio")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 10))
                    .trainingDurationMinutes(60)
                    .build();

            Training training2 = Training.builder()
                    .id(2L)
                    .trainee(testTrainee)
                    .trainer(trainer2)
                    .trainingName("Strength Training")
                    .trainingType(strengthType)
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingDurationMinutes(45)
                    .build();

            Training training3 = Training.builder()
                    .id(3L)
                    .trainee(testTrainee)
                    .trainer(trainer1)
                    .trainingName("Evening Cardio")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 20))
                    .trainingDurationMinutes(30)
                    .build();

            sampleTrainings = new ArrayList<>(List.of(training1, training2, training3));
        }

        @Test
        @DisplayName("Should return all trainings when no filters applied")
        void shouldReturnAllTrainingsWhenNoFiltersApplied() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, null, null);

            // Then
            assertThat(result).hasSize(3);
            verify(traineeService).authenticate("john.doe", "traineePass");
            verify(trainingRepository).findTrainingsWithAllUsers("john.doe", null, null, null);
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 6, 1);
            LocalDate toDate = LocalDate.of(2024, 6, 30);

            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), eq(fromDate), eq(toDate)))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", fromDate, toDate, null, null);

            // Then
            assertThat(result).hasSize(3);
            verify(trainingRepository).findTrainingsWithAllUsers("john.doe", null, fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainer first name")
        void shouldFilterByTrainerFirstName() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "Mike", null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainer().getUser().getFirstName())
                    .containsOnly("Mike");
        }

        @Test
        @DisplayName("Should filter by trainer last name")
        void shouldFilterByTrainerLastName() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "Wilson", null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainer().getUser().getLastName()).isEqualTo("Wilson");
        }

        @Test
        @DisplayName("Should filter by trainer name case insensitive")
        void shouldFilterByTrainerNameCaseInsensitive() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "mike", null);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should filter by trainer name partial match")
        void shouldFilterByTrainerNamePartialMatch() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "Wil", null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainer().getUser().getLastName()).isEqualTo("Wilson");
        }

        @Test
        @DisplayName("Should filter by training type")
        void shouldFilterByTrainingType() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, null, TrainingTypeName.CARDIO);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainingType().getTrainingTypeName())
                    .containsOnly(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should filter by trainer name and training type")
        void shouldFilterByTrainerNameAndTrainingType() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "Mike", TrainingTypeName.CARDIO);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .allMatch(t -> t.getTrainer().getUser().getFirstName().equals("Mike"))
                    .allMatch(t -> t.getTrainingType().getTrainingTypeName() == TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should return empty list when no trainings match filter")
        void shouldReturnEmptyListWhenNoTrainingsMatchFilter() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "NonExistent", null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should ignore blank trainer name filter")
        void shouldIgnoreBlankTrainerNameFilter() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "   ", null);

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(traineeService).authenticate("john.doe", "wrongPass");

            // When & Then
            assertThatThrownBy(() -> trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "wrongPass", null, null, null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verifyNoInteractions(trainingRepository);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void shouldReturnEmptyListWhenNoTrainingsFound() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, null, null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTrainerTrainingsByCriteria Tests")
    class GetTrainerTrainingsByCriteriaTests {

        private List<Training> sampleTrainings;

        @BeforeEach
        void setUp() {
            User trainee1User = User.builder()
                    .id(10L)
                    .firstName("John")
                    .lastName("Doe")
                    .username("john.doe")
                    .build();

            User trainee2User = User.builder()
                    .id(11L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("jane.smith")
                    .build();

            Trainee trainee1 = Trainee.builder()
                    .id(10L)
                    .user(trainee1User)
                    .build();

            Trainee trainee2 = Trainee.builder()
                    .id(11L)
                    .user(trainee2User)
                    .build();

            Training training1 = Training.builder()
                    .id(1L)
                    .trainee(trainee1)
                    .trainer(testTrainer)
                    .trainingName("Morning Cardio")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 10))
                    .trainingDurationMinutes(60)
                    .build();

            Training training2 = Training.builder()
                    .id(2L)
                    .trainee(trainee2)
                    .trainer(testTrainer)
                    .trainingName("Afternoon Session")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingDurationMinutes(45)
                    .build();

            Training training3 = Training.builder()
                    .id(3L)
                    .trainee(trainee1)
                    .trainer(testTrainer)
                    .trainingName("Evening Cardio")
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 20))
                    .trainingDurationMinutes(30)
                    .build();

            sampleTrainings = new ArrayList<>(List.of(training1, training2, training3));
        }

        @Test
        @DisplayName("Should return all trainings when no filters applied")
        void shouldReturnAllTrainingsWhenNoFiltersApplied() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, null);

            // Then
            assertThat(result).hasSize(3);
            verify(trainerService).authenticate("mike.johnson", "trainerPass");
            verify(trainingRepository).findTrainingsWithAllUsers(null, "mike.johnson", null, null);
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            LocalDate fromDate = LocalDate.of(2024, 6, 1);
            LocalDate toDate = LocalDate.of(2024, 6, 30);

            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), eq(fromDate), eq(toDate)))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", fromDate, toDate, null);

            // Then
            assertThat(result).hasSize(3);
            verify(trainingRepository).findTrainingsWithAllUsers(null, "mike.johnson", fromDate, toDate);
        }

        @Test
        @DisplayName("Should filter by trainee first name")
        void shouldFilterByTraineeFirstName() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "John");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainee().getUser().getFirstName())
                    .containsOnly("John");
        }

        @Test
        @DisplayName("Should filter by trainee last name")
        void shouldFilterByTraineeLastName() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "Smith");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainee().getUser().getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should filter by trainee name case insensitive")
        void shouldFilterByTraineeNameCaseInsensitive() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "JOHN");

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should filter by trainee name partial match")
        void shouldFilterByTraineeNamePartialMatch() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "Smi");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainee().getUser().getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should return empty list when no trainings match filter")
        void shouldReturnEmptyListWhenNoTrainingsMatchFilter() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "NonExistent");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should ignore blank trainee name filter")
        void shouldIgnoreBlankTraineeNameFilter() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(sampleTrainings);

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, "   ");

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(trainerService).authenticate("mike.johnson", "wrongPass");

            // When & Then
            assertThatThrownBy(() -> trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "wrongPass", null, null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verifyNoInteractions(trainingRepository);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void shouldReturnEmptyListWhenNoTrainingsFound() {
            // Given
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    isNull(), eq("mike.johnson"), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTrainerTrainingsByCriteria(
                    "mike.johnson", "trainerPass", null, null, null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllTrainingTypes Tests")
    class GetAllTrainingTypesTests {

        @Test
        @DisplayName("Should return all training types")
        void shouldReturnAllTrainingTypes() {
            // Given
            TrainingType yogaType = new TrainingType(3L, TrainingTypeName.YOGA);
            List<TrainingType> allTypes = List.of(cardioType, strengthType, yogaType);

            when(trainingTypeRepository.findAll()).thenReturn(allTypes);

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(TrainingType::getTrainingTypeName)
                    .containsExactlyInAnyOrder(
                            TrainingTypeName.CARDIO,
                            TrainingTypeName.STRENGTH,
                            TrainingTypeName.YOGA
                    );
            verify(trainingTypeRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no training types exist")
        void shouldReturnEmptyListWhenNoTrainingTypesExist() {
            // Given
            when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return single training type")
        void shouldReturnSingleTrainingType() {
            // Given
            when(trainingTypeRepository.findAll()).thenReturn(List.of(cardioType));

            // When
            List<TrainingType> result = trainingService.getAllTrainingTypes();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingTypeName()).isEqualTo(TrainingTypeName.CARDIO);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle training with minimum duration")
        void shouldHandleTrainingWithMinimumDuration() {
            // Given
            AddTrainingRequest request = new AddTrainingRequest();
            request.setTraineeUsername("john.doe");
            request.setTraineePassword("traineePass");
            request.setTrainerUsername("mike.johnson");
            request.setTrainerPassword("trainerPass");
            request.setTrainingName("Quick Session");
            request.setTrainingDate(LocalDate.of(2024, 6, 15));
            request.setTrainingDuration(1);

            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingDurationMinutes()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle training on boundary date")
        void shouldHandleTrainingOnBoundaryDate() {
            // Given
            AddTrainingRequest request = new AddTrainingRequest();
            request.setTraineeUsername("john.doe");
            request.setTraineePassword("traineePass");
            request.setTrainerUsername("mike.johnson");
            request.setTrainerPassword("trainerPass");
            request.setTrainingName("New Year Training");
            request.setTrainingDate(LocalDate.of(2024, 1, 1));
            request.setTrainingDuration(60);

            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            doNothing().when(trainerService).authenticate("mike.johnson", "trainerPass");
            when(traineeService.getByUsername("john.doe")).thenReturn(testTrainee);
            when(trainerService.getByUsername("mike.johnson")).thenReturn(testTrainer);
            when(validator.validate(any(Training.class))).thenReturn(Collections.emptySet());
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            trainingService.createTraining(request);

            // Then
            verify(trainingRepository).save(trainingCaptor.capture());
            assertThat(trainingCaptor.getValue().getTrainingDate())
                    .isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("Should filter trainings with empty name string")
        void shouldFilterTrainingsWithEmptyNameString() {
            // Given
            doNothing().when(traineeService).authenticate("john.doe", "traineePass");
            when(trainingRepository.findTrainingsWithAllUsers(
                    eq("john.doe"), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());

            // When
            List<Training> result = trainingService.getTraineeTrainingsByCriteria(
                    "john.doe", "traineePass", null, null, "", null);

            // Then
            assertThat(result).isEmpty();
        }
    }
}