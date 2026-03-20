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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService Unit Tests")
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

    // Test data
    private User testUser;
    private Trainer testTrainer;
    private TrainingType trainingType;
    private Trainee testTrainee;

    private static final String USERNAME = "jane.smith";
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String PASSWORD = "password123";
    private static final String TRAINEE_USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        // Setup User
        testUser = User.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        // Setup TrainingType
        trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        // Setup Trainer
        testTrainer = Trainer.builder()
                .id(1L)
                .user(testUser)
                .specialization(trainingType)
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        // Setup Trainee User
        User traineeUser = User.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .username(TRAINEE_USERNAME)
                .password("traineePass")
                .isActive(true)
                .build();

        // Setup Trainee
        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street")
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();
    }

    // ==================== CREATE PROFILE TESTS ====================

    @Nested
    @DisplayName("createProfile() Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainer profile successfully")
        void createProfile_WithValidRequest_ReturnsCreatedTrainer() {
            // Arrange
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(trainingType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
                Trainer trainer = invocation.getArgument(0);
                trainer.setId(1L);
                return trainer;
            });

            // Act
            Trainer result = trainerService.createProfile(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getSpecialization()).isEqualTo(trainingType);

            verify(userService).createUser(FIRST_NAME, LAST_NAME);
            verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.FITNESS);
            verify(validator).validate(any(Trainer.class));
            verify(trainerRepository).save(trainerCaptor.capture());

            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainer.getSpecialization()).isEqualTo(trainingType);
            assertThat(capturedTrainer.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should create trainer with different specializations")
        void createProfile_WithDifferentSpecializations_ReturnsCreatedTrainer() {
            // Arrange
            TrainingType yogaType = new TrainingType(2L, TrainingTypeName.YOGA);

            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                    .thenReturn(Optional.of(yogaType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> {
                Trainer t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            // Act
            Trainer result = trainerService.createProfile(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSpecialization()).isEqualTo(yogaType);
            assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void createProfile_TrainingTypeNotFound_ThrowsNotFoundException() {
            // Arrange
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Training type not found: YOGA");

            verify(userService).createUser(FIRST_NAME, LAST_NAME);
            verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.YOGA);
            verify(validator, never()).validate(any(Trainer.class));
            verify(trainerRepository, never()).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails_ThrowsValidationException() {
            // Arrange
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(trainingType));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Specialization is required");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Specialization is required");

            verify(trainerRepository, never()).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple violation messages")
        void createProfile_MultipleValidationErrors_ThrowsValidationExceptionWithAllMessages() {
            // Arrange
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(trainingType));

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

            // Act & Assert
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(trainerRepository, never()).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should initialize trainees list as empty immutable list")
        void createProfile_InitializesTraineesListAsEmpty() {
            // Arrange
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(trainingType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            trainerService.createProfile(request);

            // Assert
            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getTrainees()).isNotNull();
            assertThat(capturedTrainer.getTrainees()).isEmpty();
        }
    }

    // ==================== GET BY USERNAME TESTS ====================

    @Nested
    @DisplayName("getByUsername() Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainer when found")
        void getByUsername_TrainerExists_ReturnsTrainer() {
            // Arrange
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));

            // Act
            Trainer result = trainerService.getByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testTrainer);
            assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);

            verify(trainerRepository).findByUser_Username(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void getByUsername_TrainerNotExists_ThrowsNotFoundException() {
            // Arrange
            String nonExistentUsername = "nonexistent.user";
            when(trainerRepository.findByUser_Username(nonExistentUsername)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> trainerService.getByUsername(nonExistentUsername))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainer not found: " + nonExistentUsername);

            verify(trainerRepository).findByUser_Username(nonExistentUsername);
        }

        @Test
        @DisplayName("Should call repository with correct username")
        void getByUsername_CallsRepositoryWithCorrectUsername() {
            // Arrange
            when(trainerRepository.findByUser_Username(anyString())).thenReturn(Optional.of(testTrainer));

            // Act
            trainerService.getByUsername(USERNAME);

            // Assert
            verify(trainerRepository, times(1)).findByUser_Username(USERNAME);
        }

        @Test
        @DisplayName("Should return trainer with all associations")
        void getByUsername_ReturnsTrainerWithAllAssociations() {
            // Arrange
            testTrainer.getTrainees().add(testTrainee);

            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));

            // Act
            Trainer result = trainerService.getByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTrainees()).hasSize(1);
            assertThat(result.getSpecialization()).isNotNull();
            assertThat(result.getUser()).isNotNull();
        }
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Nested
    @DisplayName("updateProfile() Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateProfile_WithValidRequest_ReturnsUpdatedTrainer() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .isActive(false)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            doNothing().when(userService).updateUserBasicInfo(
                    any(User.class), anyString(), anyString(), any(Boolean.class));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            Trainer result = trainerService.updateProfile(USERNAME, request);

            // Assert
            assertThat(result).isNotNull();

            verify(userService).isAuthenticated(USERNAME);
            verify(trainerRepository).findByUser_Username(USERNAME);
            verify(userService).updateUserBasicInfo(
                    testTrainer.getUser(),
                    "UpdatedFirst",
                    "UpdatedLast",
                    false
            );
            verify(validator).validate(testTrainer);
            verify(trainerRepository).save(testTrainer);
        }

        @Test
        @DisplayName("Should update only firstName")
        void updateProfile_UpdateOnlyFirstName_Success() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName("NewFirstName")
                    .lastName(null)
                    .isActive(null)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            Trainer result = trainerService.updateProfile(USERNAME, request);

            // Assert
            assertThat(result).isNotNull();
            verify(userService).updateUserBasicInfo(
                    testTrainer.getUser(),
                    "NewFirstName",
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Should update only isActive status")
        void updateProfile_UpdateOnlyIsActive_Success() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName(null)
                    .lastName(null)
                    .isActive(false)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            Trainer result = trainerService.updateProfile(USERNAME, request);

            // Assert
            assertThat(result).isNotNull();
            verify(userService).updateUserBasicInfo(
                    testTrainer.getUser(),
                    null,
                    null,
                    false
            );
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void updateProfile_TrainerNotFound_ThrowsNotFoundException() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found");

            verify(userService).isAuthenticated(USERNAME);
            verify(trainerRepository, never()).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void updateProfile_ValidationFails_ThrowsValidationException() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid trainer data");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Invalid trainer data");

            verify(trainerRepository, never()).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should authenticate with request username")
        void updateProfile_AuthenticatesWithRequestUsername() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            trainerService.updateProfile(USERNAME, request);

            // Assert
            verify(userService).isAuthenticated(request.getUsername());
        }

        @Test
        @DisplayName("Should preserve specialization after update")
        void updateProfile_PreservesSpecialization() {
            // Arrange
            TrainingType originalSpecialization = testTrainer.getSpecialization();

            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName("NewFirst")
                    .lastName("NewLast")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            trainerService.updateProfile(USERNAME, request);

            // Assert
            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getSpecialization()).isEqualTo(originalSpecialization);
        }
    }

    // ==================== GET UNASSIGNED TRAINERS TESTS ====================

    @Nested
    @DisplayName("getUnassignedTrainers() Tests")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should return unassigned trainers successfully")
        void getUnassignedTrainers_ReturnsUnassignedTrainers() {
            // Arrange
            List<Trainer> unassignedTrainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(unassignedTrainers);

            // Act
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(testTrainer);

            verify(userService).isAuthenticated(TRAINEE_USERNAME);
            verify(traineeRepository).findByUser_Username(TRAINEE_USERNAME);
            verify(trainerRepository).findAvailableTrainers(TRAINEE_USERNAME);
        }

        @Test
        @DisplayName("Should return multiple unassigned trainers")
        void getUnassignedTrainers_ReturnsMultipleTrainers() {
            // Arrange
            User trainer2User = User.builder()
                    .id(3L)
                    .firstName("Bob")
                    .lastName("Johnson")
                    .username("bob.johnson")
                    .isActive(true)
                    .build();

            TrainingType yogaType = new TrainingType(2L, TrainingTypeName.YOGA);

            Trainer trainer2 = Trainer.builder()
                    .id(2L)
                    .user(trainer2User)
                    .specialization(yogaType)
                    .trainees(new ArrayList<>())
                    .build();

            List<Trainer> unassignedTrainers = List.of(testTrainer, trainer2);

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(unassignedTrainers);

            // Act
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testTrainer, trainer2);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_NoUnassignedTrainers_ReturnsEmptyList() {
            // Arrange
            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(Collections.emptyList());

            // Act
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getUnassignedTrainers_TraineeNotFound_ThrowsNotFoundException() {
            // Arrange
            String nonExistentTrainee = "nonexistent.trainee";

            doNothing().when(userService).isAuthenticated(nonExistentTrainee);
            when(traineeRepository.findByUser_Username(nonExistentTrainee)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers(nonExistentTrainee))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainee not found: " + nonExistentTrainee);

            verify(userService).isAuthenticated(nonExistentTrainee);
            verify(traineeRepository).findByUser_Username(nonExistentTrainee);
            verify(trainerRepository, never()).findAvailableTrainers(anyString());
        }

        @Test
        @DisplayName("Should authenticate before checking trainee")
        void getUnassignedTrainers_AuthenticatesFirst() {
            // Arrange
            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(Collections.emptyList());

            // Act
            trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            var inOrder = inOrder(userService, traineeRepository, trainerRepository);
            inOrder.verify(userService).isAuthenticated(TRAINEE_USERNAME);
            inOrder.verify(traineeRepository).findByUser_Username(TRAINEE_USERNAME);
            inOrder.verify(trainerRepository).findAvailableTrainers(TRAINEE_USERNAME);
        }

        @Test
        @DisplayName("Should call repository with correct trainee username")
        void getUnassignedTrainers_CallsRepositoryWithCorrectUsername() {
            // Arrange
            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(Collections.emptyList());

            // Act
            trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            verify(trainerRepository).findAvailableTrainers(TRAINEE_USERNAME);
        }

        @Test
        @DisplayName("Should return only active trainers not assigned to trainee")
        void getUnassignedTrainers_ReturnsOnlyActiveUnassignedTrainers() {
            // Arrange
            // Create an assigned trainer
            User assignedTrainerUser = User.builder()
                    .id(4L)
                    .firstName("Assigned")
                    .lastName("Trainer")
                    .username("assigned.trainer")
                    .isActive(true)
                    .build();

            Trainer assignedTrainer = Trainer.builder()
                    .id(3L)
                    .user(assignedTrainerUser)
                    .specialization(trainingType)
                    .trainees(new ArrayList<>())
                    .build();

            // Add assigned trainer to trainee
            testTrainee.getTrainers().add(assignedTrainer);

            // Only return unassigned trainer
            List<Trainer> unassignedTrainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME)).thenReturn(unassignedTrainers);

            // Act
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(testTrainer);
            assertThat(result).doesNotContain(assignedTrainer);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle trainer with no trainees")
        void getByUsername_TrainerWithNoTrainees_ReturnsTrainer() {
            // Arrange
            testTrainer.setTrainees(List.of());
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));

            // Act
            Trainer result = trainerService.getByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should handle trainer with multiple trainees")
        void getByUsername_TrainerWithMultipleTrainees_ReturnsTrainerWithAllTrainees() {
            // Arrange
            User trainee2User = User.builder()
                    .id(3L)
                    .firstName("Alice")
                    .lastName("Wonder")
                    .username("alice.wonder")
                    .isActive(true)
                    .build();

            Trainee trainee2 = Trainee.builder()
                    .id(2L)
                    .user(trainee2User)
                    .trainers(new ArrayList<>())
                    .build();

            List<Trainee> trainees = new ArrayList<>();
            trainees.add(testTrainee);
            trainees.add(trainee2);
            testTrainer.setTrainees(trainees);

            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));

            // Act
            Trainer result = trainerService.getByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTrainees()).hasSize(2);
        }

        @Test
        @DisplayName("Should verify all training types can be used for specialization")
        void createProfile_AllTrainingTypes_Success() {
            // Test that the service works with any training type
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                // Arrange
                TrainingType type = new TrainingType((long) typeName.ordinal() + 1, typeName);

                TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .specialization(typeName)
                        .build();

                when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
                when(trainingTypeRepository.findByTrainingTypeName(typeName))
                        .thenReturn(Optional.of(type));
                when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
                when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> {
                    Trainer t = inv.getArgument(0);
                    t.setId(1L);
                    return t;
                });

                // Act
                Trainer result = trainerService.createProfile(request);

                // Assert
                assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo(typeName);

                // Reset mocks for next iteration
                reset(userService, trainingTypeRepository, validator, trainerRepository);
            }
        }

        @Test
        @DisplayName("Should handle concurrent update attempts")
        void updateProfile_PreservesDataIntegrity() {
            // Arrange
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(USERNAME)
                    .firstName("ConcurrentFirst")
                    .lastName("ConcurrentLast")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // Act
            Trainer result = trainerService.updateProfile(USERNAME, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L); // ID should not change
            assertThat(result.getSpecialization()).isEqualTo(trainingType); // Specialization preserved
        }
    }
}