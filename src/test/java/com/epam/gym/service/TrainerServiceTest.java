package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService Tests")
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @Mock
    private Validator validator;

    @InjectMocks
    private TrainerService trainerService;

    @Captor
    private ArgumentCaptor<Trainer> trainerCaptor;

    private User testUser;
    private Trainer testTrainer;
    private TrainingType cardioType;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Mike")
                .lastName("Johnson")
                .username("mike.johnson")
                .password("password123")
                .isActive(true)
                .build();

        cardioType = new TrainingType(1L, TrainingTypeName.CARDIO);

        testTrainer = Trainer.builder()
                .id(1L)
                .user(testUser)
                .specialization(cardioType)
                .trainees(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("createProfile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainer profile successfully")
        void shouldCreateTrainerProfileSuccessfully() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.CARDIO);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CARDIO))
                    .thenReturn(Optional.of(cardioType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            Trainer result = trainerService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser().getUsername()).isEqualTo("mike.johnson");
            assertThat(result.getSpecialization()).isEqualTo(cardioType);

            verify(userService).createUser("Mike", "Johnson");
            verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.CARDIO);
            verify(validator).validate(any(Trainer.class));
            verify(trainerRepository).save(trainerCaptor.capture());

            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainer.getSpecialization()).isEqualTo(cardioType);
        }

        @Test
        @DisplayName("Should create trainer with different specialization")
        void shouldCreateTrainerWithDifferentSpecialization() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Sarah");
            request.setLastName("Wilson");
            request.setSpecialization(TrainingTypeName.STRENGTH);

            TrainingType strengthType = new TrainingType(2L, TrainingTypeName.STRENGTH);

            User user = User.builder()
                    .id(2L)
                    .firstName("Sarah")
                    .lastName("Wilson")
                    .username("sarah.wilson")
                    .password("pass123")
                    .isActive(true)
                    .build();

            Trainer savedTrainer = Trainer.builder()
                    .id(2L)
                    .user(user)
                    .specialization(strengthType)
                    .trainees(List.of())
                    .build();

            when(userService.createUser("Sarah", "Wilson")).thenReturn(user);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.STRENGTH))
                    .thenReturn(Optional.of(strengthType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(savedTrainer);

            // When
            Trainer result = trainerService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSpecialization().getTrainingTypeName())
                    .isEqualTo(TrainingTypeName.STRENGTH);
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void shouldThrowNotFoundExceptionWhenTrainingTypeNotFound() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.YOGA);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found: YOGA");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void shouldThrowValidationExceptionWhenValidationFails() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.CARDIO);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CARDIO))
                    .thenReturn(Optional.of(cardioType));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Specialization is required");
            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation);

            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Specialization is required");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should initialize trainees list as empty")
        void shouldInitializeTraineesListAsEmpty() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.CARDIO);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CARDIO))
                    .thenReturn(Optional.of(cardioType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            trainerService.createProfile(request);

            // Then
            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getTrainees()).isNotNull();
            assertThat(capturedTrainer.getTrainees()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByUsername Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainer when found")
        void shouldReturnTrainerWhenFound() {
            // Given
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("mike.johnson");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo("mike.johnson");
            assertThat(result.getSpecialization()).isEqualTo(cardioType);
            verify(trainerRepository).findByUser_Username("mike.johnson");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void shouldThrowNotFoundExceptionWhenTrainerNotFound() {
            // Given
            when(trainerRepository.findByUser_Username("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getByUsername("nonexistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found: nonexistent");
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        private UpdateTrainerRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTrainerRequest();
            updateRequest.setPassword("password123");
            updateRequest.setFirstName("Michael");
            updateRequest.setLastName("Updated");
            updateRequest.setIsActive(true);
        }

        @Test
        @DisplayName("Should update trainer profile successfully")
        void shouldUpdateTrainerProfileSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("mike.johnson", "password123");
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            Trainer result = trainerService.updateProfile("mike.johnson", updateRequest);

            // Then
            assertThat(result).isNotNull();

            verify(userService).authenticate("mike.johnson", "password123");
            verify(userService).updateUserBasicInfo(testUser, "Michael", "Updated", true);
            verify(validator).validate(any(Trainer.class));
            verify(trainerRepository).save(testTrainer);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            updateRequest.setPassword("wrongpassword");
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("mike.johnson", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile("mike.johnson", updateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(trainerRepository, never()).findByUser_Username(anyString());
            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void shouldThrowNotFoundExceptionWhenTrainerNotFound() {
            // Given
            doNothing().when(userService).authenticate("nonexistent", "password123");
            when(trainerRepository.findByUser_Username("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile("nonexistent", updateRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found: nonexistent");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails on update")
        void shouldThrowValidationExceptionWhenValidationFailsOnUpdate() {
            // Given
            doNothing().when(userService).authenticate("mike.johnson", "password123");
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid trainer data");
            Set<ConstraintViolation<Trainer>> violations = Set.of(violation);

            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile("mike.johnson", updateRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Invalid trainer data");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update isActive status")
        void shouldUpdateIsActiveStatus() {
            // Given
            updateRequest.setIsActive(false);

            doNothing().when(userService).authenticate("mike.johnson", "password123");
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            trainerService.updateProfile("mike.johnson", updateRequest);

            // Then
            verify(userService).updateUserBasicInfo(testUser, "Michael", "Updated", false);
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate trainer successfully")
        void shouldAuthenticateTrainerSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("mike.johnson", "password123");
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            trainerService.authenticate("mike.johnson", "password123");

            // Then
            verify(userService).authenticate("mike.johnson", "password123");
            verify(trainerRepository).findByUser_Username("mike.johnson");
        }

        @Test
        @DisplayName("Should throw exception when user authentication fails")
        void shouldThrowExceptionWhenUserAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("mike.johnson", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> trainerService.authenticate("mike.johnson", "wrongpassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(trainerRepository, never()).findByUser_Username(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user exists but not a trainer")
        void shouldThrowNotFoundExceptionWhenUserExistsButNotATrainer() {
            // Given
            doNothing().when(userService).authenticate("trainee.user", "password123");
            when(trainerRepository.findByUser_Username("trainee.user"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.authenticate("trainee.user", "password123"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found: trainee.user");
        }
    }

    @Nested
    @DisplayName("getUnassignedTrainers Tests")
    class GetUnassignedTrainersTests {

        private Trainee testTrainee;
        private List<Trainer> availableTrainers;

        @BeforeEach
        void setUp() {
            User traineeUser = User.builder()
                    .id(10L)
                    .firstName("John")
                    .lastName("Doe")
                    .username("john.doe")
                    .password("password123")
                    .isActive(true)
                    .build();

            testTrainee = Trainee.builder()
                    .id(10L)
                    .user(traineeUser)
                    .build();

            User trainerUser1 = User.builder()
                    .id(20L)
                    .username("trainer1")
                    .firstName("Trainer")
                    .lastName("One")
                    .build();

            User trainerUser2 = User.builder()
                    .id(21L)
                    .username("trainer2")
                    .firstName("Trainer")
                    .lastName("Two")
                    .build();

            TrainingType strengthType = new TrainingType(2L, TrainingTypeName.STRENGTH);

            Trainer trainer1 = Trainer.builder()
                    .id(20L)
                    .user(trainerUser1)
                    .specialization(cardioType)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(21L)
                    .user(trainerUser2)
                    .specialization(strengthType)
                    .build();

            availableTrainers = List.of(trainer1, trainer2);
        }

        @Test
        @DisplayName("Should return unassigned trainers successfully")
        void shouldReturnUnassignedTrainersSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("john.doe"))
                    .thenReturn(availableTrainers);

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("john.doe", "password123");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer2");

            verify(userService).authenticate("john.doe", "password123");
            verify(traineeRepository).findByUser_Username("john.doe");
            verify(trainerRepository).findAvailableTrainers("john.doe");
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void shouldReturnEmptyListWhenNoUnassignedTrainers() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("john.doe"))
                    .thenReturn(Collections.emptyList());

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("john.doe", "password123");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("john.doe", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers("john.doe", "wrongpassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verifyNoInteractions(traineeRepository);
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void shouldThrowNotFoundExceptionWhenTraineeNotFound() {
            // Given
            doNothing().when(userService).authenticate("nonexistent", "password123");
            when(traineeRepository.findByUser_Username("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers("nonexistent", "password123"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: nonexistent");

            verify(trainerRepository, never()).findAvailableTrainers(anyString());
        }

        @Test
        @DisplayName("Should return trainers with different specializations")
        void shouldReturnTrainersWithDifferentSpecializations() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("john.doe"))
                    .thenReturn(availableTrainers);

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("john.doe", "password123");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getSpecialization().getTrainingTypeName())
                    .containsExactlyInAnyOrder(TrainingTypeName.CARDIO, TrainingTypeName.STRENGTH);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.CARDIO);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CARDIO))
                    .thenReturn(Optional.of(cardioType));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Error 1");

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("Error 2");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);

            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should pass validation when no violations")
        void shouldPassValidationWhenNoViolations() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Johnson");
            request.setSpecialization(TrainingTypeName.CARDIO);

            when(userService.createUser("Mike", "Johnson")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CARDIO))
                    .thenReturn(Optional.of(cardioType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            Trainer result = trainerService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            verify(trainerRepository).save(any(Trainer.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle trainer with no trainees")
        void shouldHandleTrainerWithNoTrainees() {
            // Given
            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("mike.johnson");

            // Then
            assertThat(result.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should handle trainer with existing trainees")
        void shouldHandleTrainerWithExistingTrainees() {
            // Given
            User traineeUser = User.builder()
                    .id(100L)
                    .username("trainee1")
                    .build();

            Trainee trainee = Trainee.builder()
                    .id(100L)
                    .user(traineeUser)
                    .build();

            testTrainer.setTrainees(List.of(trainee));

            when(trainerRepository.findByUser_Username("mike.johnson"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("mike.johnson");

            // Then
            assertThat(result.getTrainees()).hasSize(1);
            assertThat(result.getTrainees().getFirst().getUser().getUsername()).isEqualTo("trainee1");
        }
    }
}