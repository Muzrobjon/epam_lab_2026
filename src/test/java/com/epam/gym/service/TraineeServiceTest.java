package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
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
@DisplayName("TraineeService Unit Tests")
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private Validator validator;

    @InjectMocks
    private TraineeService traineeService;

    @Captor
    private ArgumentCaptor<Trainee> traineeCaptor;

    // Test data
    private User testUser;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType trainingType;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PASSWORD = "password123";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main Street";

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

        // Setup Trainee
        testTrainee = Trainee.builder()
                .id(1L)
                .user(testUser)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        // Setup TrainingType (no builder - use constructor)
        trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        // Setup Trainer User
        User trainerUser = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("trainerPass")
                .isActive(true)
                .build();

        // Setup Trainer
        testTrainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();
    }

    // ==================== CREATE PROFILE TESTS ====================

    @Nested
    @DisplayName("createProfile() Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainee profile with all fields successfully")
        void createProfile_WithAllFields_ReturnsCreatedTrainee() {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .dateOfBirth(DATE_OF_BIRTH)
                    .address(ADDRESS)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee trainee = invocation.getArgument(0);
                trainee.setId(1L);
                return trainee;
            });

            // Act
            Trainee result = traineeService.createProfile(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
            assertThat(result.getAddress()).isEqualTo(ADDRESS);
            assertThat(result.getTrainers()).isEmpty();

            verify(userService).createUser(FIRST_NAME, LAST_NAME);
            verify(validator).validate(any(Trainee.class));
            verify(traineeRepository).save(traineeCaptor.capture());

            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainee.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
            assertThat(capturedTrainee.getAddress()).isEqualTo(ADDRESS);
        }

        @Test
        @DisplayName("Should create trainee profile without optional fields")
        void createProfile_WithoutOptionalFields_ReturnsCreatedTrainee() {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> {
                Trainee t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });

            // Act
            Trainee result = traineeService.createProfile(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isNull();
            assertThat(result.getAddress()).isNull();

            verify(userService).createUser(FIRST_NAME, LAST_NAME);
            verify(traineeRepository).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails_ThrowsValidationException() {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);

            // Create mock violation
            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User must not be null");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User must not be null");

            verify(userService).createUser(FIRST_NAME, LAST_NAME);
            verify(validator).validate(any(Trainee.class));
            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple violation messages")
        void createProfile_MultipleValidationErrors_ThrowsValidationExceptionWithAllMessages() {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Error 1");

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("Error 2");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should initialize trainers list as empty ArrayList")
        void createProfile_InitializesTrainersListAsEmpty() {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            when(userService.createUser(FIRST_NAME, LAST_NAME)).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            traineeService.createProfile(request);

            // Assert
            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).isNotNull();
            assertThat(capturedTrainee.getTrainers()).isEmpty();
        }
    }

    // ==================== GET BY USERNAME TESTS ====================

    @Nested
    @DisplayName("getByUsername() Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainee when found")
        void getByUsername_TraineeExists_ReturnsTrainee() {
            // Arrange
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));

            // Act
            Trainee result = traineeService.getByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testTrainee);
            assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);

            verify(traineeRepository).findByUser_Username(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getByUsername_TraineeNotExists_ThrowsNotFoundException() {
            // Arrange
            String nonExistentUsername = "nonexistent.user";
            when(traineeRepository.findByUser_Username(nonExistentUsername)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> traineeService.getByUsername(nonExistentUsername))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainee not found: " + nonExistentUsername);

            verify(traineeRepository).findByUser_Username(nonExistentUsername);
        }

        @Test
        @DisplayName("Should call repository with correct username")
        void getByUsername_CallsRepositoryWithCorrectUsername() {
            // Arrange
            when(traineeRepository.findByUser_Username(anyString())).thenReturn(Optional.of(testTrainee));

            // Act
            traineeService.getByUsername(USERNAME);

            // Assert
            verify(traineeRepository, times(1)).findByUser_Username(USERNAME);
        }
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Nested
    @DisplayName("updateProfile() Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainee profile with all fields")
        void updateProfile_WithAllFields_ReturnsUpdatedTrainee() {
            // Arrange
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .isActive(false)
                    .dateOfBirth(LocalDate.of(1992, 8, 20))
                    .address("456 New Street")
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            doNothing().when(userService).updateUserBasicInfo(any(User.class), anyString(), anyString(), any(Boolean.class));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            Trainee result = traineeService.updateProfile(USERNAME, request);

            // Assert
            assertThat(result).isNotNull();

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(userService).updateUserBasicInfo(
                    testTrainee.getUser(),
                    "UpdatedFirst",
                    "UpdatedLast",
                    false
            );
            verify(validator).validate(testTrainee);
            verify(traineeRepository).save(testTrainee);

            assertThat(testTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1992, 8, 20));
            assertThat(testTrainee.getAddress()).isEqualTo("456 New Street");
        }

        @Test
        @DisplayName("Should not update dateOfBirth when null in request")
        void updateProfile_NullDateOfBirth_KeepsOriginalDateOfBirth() {
            // Arrange
            LocalDate originalDateOfBirth = testTrainee.getDateOfBirth();

            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .dateOfBirth(null)
                    .address("New Address")
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            traineeService.updateProfile(USERNAME, request);

            // Assert
            assertThat(testTrainee.getDateOfBirth()).isEqualTo(originalDateOfBirth);
            assertThat(testTrainee.getAddress()).isEqualTo("New Address");
        }

        @Test
        @DisplayName("Should not update address when null in request")
        void updateProfile_NullAddress_KeepsOriginalAddress() {
            // Arrange
            String originalAddress = testTrainee.getAddress();

            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .dateOfBirth(LocalDate.of(1995, 1, 1))
                    .address(null)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            traineeService.updateProfile(USERNAME, request);

            // Assert
            assertThat(testTrainee.getAddress()).isEqualTo(originalAddress);
            assertThat(testTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 1, 1));
        }

        @Test
        @DisplayName("Should not update optional fields when both are null")
        void updateProfile_BothOptionalFieldsNull_KeepsOriginalValues() {
            // Arrange
            LocalDate originalDateOfBirth = testTrainee.getDateOfBirth();
            String originalAddress = testTrainee.getAddress();

            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .dateOfBirth(null)
                    .address(null)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            traineeService.updateProfile(USERNAME, request);

            // Assert
            assertThat(testTrainee.getDateOfBirth()).isEqualTo(originalDateOfBirth);
            assertThat(testTrainee.getAddress()).isEqualTo(originalAddress);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void updateProfile_TraineeNotFound_ThrowsNotFoundException() {
            // Arrange
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void updateProfile_ValidationFails_ThrowsValidationException() {
            // Arrange
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid data");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // Act & Assert
            assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Invalid data");

            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should authenticate with request username")
        void updateProfile_AuthenticatesWithRequestUsername() {
            // Arrange
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            traineeService.updateProfile(USERNAME, request);

            // Assert
            verify(userService).isAuthenticated(request.getUsername());
        }
    }

    // ==================== DELETE BY USERNAME TESTS ====================

    @Nested
    @DisplayName("deleteByUsername() Tests")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Should delete trainee successfully")
        void deleteByUsername_TraineeExists_DeletesTrainee() {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            doNothing().when(traineeRepository).delete(testTrainee);

            // Act
            traineeService.deleteByUsername(USERNAME);

            // Assert
            verify(userService).isAuthenticated(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(traineeRepository).delete(testTrainee);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void deleteByUsername_TraineeNotFound_ThrowsNotFoundException() {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> traineeService.deleteByUsername(USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(traineeRepository, never()).delete(any(Trainee.class));
        }

        @Test
        @DisplayName("Should authenticate before deleting")
        void deleteByUsername_AuthenticatesFirst() {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            doNothing().when(traineeRepository).delete(testTrainee);

            // Act
            traineeService.deleteByUsername(USERNAME);

            // Assert
            var inOrder = inOrder(userService, traineeRepository);
            inOrder.verify(userService).isAuthenticated(USERNAME);
            inOrder.verify(traineeRepository).findByUser_Username(USERNAME);
            inOrder.verify(traineeRepository).delete(testTrainee);
        }
    }

    // ==================== UPDATE TRAINERS LIST TESTS ====================

    @Nested
    @DisplayName("updateTrainersList() Tests")
    class UpdateTrainersListTests {

        @Test
        @DisplayName("Should update trainers list successfully")
        void updateTrainersList_WithValidTrainers_ReturnsUpdatedList() {
            // Arrange
            List<String> trainerUsernames = List.of("jane.smith");
            List<Trainer> trainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(testTrainer);

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(trainerRepository).findByUser_UsernameIn(trainerUsernames);
            verify(traineeRepository).save(testTrainee);
        }

        @Test
        @DisplayName("Should update with multiple trainers")
        void updateTrainersList_WithMultipleTrainers_ReturnsAllTrainers() {
            // Arrange
            User trainer2User = User.builder()
                    .id(3L)
                    .firstName("Bob")
                    .lastName("Johnson")
                    .username("bob.johnson")
                    .isActive(true)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(2L)
                    .user(trainer2User)
                    .specialization(trainingType)
                    .build();

            List<String> trainerUsernames = List.of("jane.smith", "bob.johnson");
            List<Trainer> trainers = List.of(testTrainer, trainer2);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testTrainer, trainer2);
        }

        @Test
        @DisplayName("Should clear trainers list when empty list provided")
        void updateTrainersList_WithEmptyList_ClearsTrainersList() {
            // Arrange
            testTrainee.getTrainers().add(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, Collections.emptyList());

            // Assert
            assertThat(result).isEmpty();

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when null provided")
        void updateTrainersList_WithNullList_ReturnsEmptyList() {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, null);

            // Assert
            assertThat(result).isEmpty();

            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
            verify(traineeRepository).save(testTrainee);
        }

        @Test
        @DisplayName("Should throw NotFoundException when some trainers not found")
        void updateTrainersList_SomeTrainersNotFound_ThrowsNotFoundException() {
            // Arrange
            List<String> trainerUsernames = List.of("jane.smith", "nonexistent.trainer");
            List<Trainer> foundTrainers = List.of(testTrainer); // Only one found

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(foundTrainers);

            // Act & Assert
            assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("nonexistent.trainer");

            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when all trainers not found")
        void updateTrainersList_AllTrainersNotFound_ThrowsNotFoundException() {
            // Arrange
            List<String> trainerUsernames = List.of("nonexistent1", "nonexistent2");

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("nonexistent1")
                    .hasMessageContaining("nonexistent2");

            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void updateTrainersList_TraineeNotFound_ThrowsNotFoundException() {
            // Arrange
            List<String> trainerUsernames = List.of("jane.smith");

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
            verify(traineeRepository, never()).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should clear existing trainers before adding new ones")
        void updateTrainersList_ClearsExistingTrainersBeforeAdding() {
            // Arrange
            User existingTrainerUser = User.builder()
                    .id(4L)
                    .firstName("Existing")
                    .lastName("Trainer")
                    .username("existing.trainer")
                    .isActive(true)
                    .build();

            Trainer existingTrainer = Trainer.builder()
                    .id(3L)
                    .user(existingTrainerUser)
                    .specialization(trainingType)
                    .build();

            testTrainee.getTrainers().add(existingTrainer);

            List<String> newTrainerUsernames = List.of("jane.smith");
            List<Trainer> newTrainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(newTrainerUsernames)).thenReturn(newTrainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, newTrainerUsernames);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result).contains(testTrainer);
            assertThat(result).doesNotContain(existingTrainer);

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).hasSize(1);
            assertThat(capturedTrainee.getTrainers()).contains(testTrainer);
        }

        @Test
        @DisplayName("Should authenticate before updating trainers list")
        void updateTrainersList_AuthenticatesFirst() {
            // Arrange
            List<String> trainerUsernames = List.of("jane.smith");
            List<Trainer> trainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // Act
            traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Assert
            var inOrder = inOrder(userService, traineeRepository, trainerRepository);
            inOrder.verify(userService).isAuthenticated(USERNAME);
            inOrder.verify(traineeRepository).findByUser_Username(USERNAME);
            inOrder.verify(trainerRepository).findByUser_UsernameIn(trainerUsernames);
            inOrder.verify(traineeRepository).save(testTrainee);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle trainee with null trainers list")
        void updateTrainersList_TraineeWithNullTrainersList_InitializesAndUpdates() {
            // Arrange
            Trainee traineeWithNullTrainers = Trainee.builder()
                    .id(1L)
                    .user(testUser)
                    .dateOfBirth(DATE_OF_BIRTH)
                    .address(ADDRESS)
                    .trainers(new ArrayList<>()) // Empty list
                    .build();

            List<String> trainerUsernames = List.of("jane.smith");
            List<Trainer> trainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(traineeWithNullTrainers));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(traineeWithNullTrainers);

            // Act
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Assert
            assertThat(result).hasSize(1);
            verify(traineeRepository).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should handle duplicate trainer usernames in list")
        void updateTrainersList_DuplicateTrainerUsernames_HandlesCorrectly() {
            // Arrange
            List<String> trainerUsernames = List.of("jane.smith", "jane.smith");
            // Repository returns only one trainer for duplicate usernames
            List<Trainer> trainers = List.of(testTrainer);

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainers);

            // Act & Assert - should throw because found count doesn't match request count
            assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}