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
@DisplayName("TraineeService Tests")
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

    private User testUser;
    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        testTrainee = Trainee.builder()
                .id(1L)
                .user(testUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("createProfile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainee profile successfully")
        void shouldCreateTraineeProfileSuccessfully() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setDateOfBirth(LocalDate.of(1990, 5, 15));
            request.setAddress("123 Main St");

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser().getUsername()).isEqualTo("john.doe");

            verify(userService).createUser("John", "Doe");
            verify(validator).validate(any(Trainee.class));
            verify(traineeRepository).save(traineeCaptor.capture());

            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(capturedTrainee.getAddress()).isEqualTo("123 Main St");
        }

        @Test
        @DisplayName("Should create trainee profile without optional fields")
        void shouldCreateTraineeProfileWithoutOptionalFields() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            User user = User.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("jane.smith")
                    .password("pass123")
                    .isActive(true)
                    .build();

            Trainee savedTrainee = Trainee.builder()
                    .id(2L)
                    .user(user)
                    .trainers(new ArrayList<>())
                    .build();

            when(userService.createUser("Jane", "Smith")).thenReturn(user);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(savedTrainee);

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isNull();
            assertThat(result.getAddress()).isNull();

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getDateOfBirth()).isNull();
            assertThat(capturedTrainee.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void shouldThrowValidationExceptionWhenValidationFails() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");

            when(userService.createUser("John", "Doe")).thenReturn(testUser);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User is required");
            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation);

            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User is required");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should initialize trainers list as empty ArrayList")
        void shouldInitializeTrainersListAsEmptyArrayList() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.createProfile(request);

            // Then
            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).isNotNull();
            assertThat(capturedTrainee.getTrainers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByUsername Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainee when found")
        void shouldReturnTraineeWhenFound() {
            // Given
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            Trainee result = traineeService.getByUsername("john.doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo("john.doe");
            verify(traineeRepository).findByUser_Username("john.doe");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void shouldThrowNotFoundExceptionWhenTraineeNotFound() {
            // Given
            when(traineeRepository.findByUser_Username("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.getByUsername("nonexistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: nonexistent");
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        private UpdateTraineeRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTraineeRequest();
            updateRequest.setPassword("password123");
            updateRequest.setFirstName("Johnny");
            updateRequest.setLastName("Updated");
            updateRequest.setIsActive(true);
            updateRequest.setDateOfBirth(LocalDate.of(1991, 6, 20));
            updateRequest.setAddress("456 New St");
        }

        @Test
        @DisplayName("Should update trainee profile successfully")
        void shouldUpdateTraineeProfileSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            Trainee result = traineeService.updateProfile("john.doe", updateRequest);

            // Then
            assertThat(result).isNotNull();

            verify(userService).authenticate("john.doe", "password123");
            verify(userService).updateUserBasicInfo(testUser, "Johnny", "Updated", true);
            verify(traineeRepository).save(traineeCaptor.capture());

            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getDateOfBirth()).isEqualTo(LocalDate.of(1991, 6, 20));
            assertThat(capturedTrainee.getAddress()).isEqualTo("456 New St");
        }

        @Test
        @DisplayName("Should not update dateOfBirth when null in request")
        void shouldNotUpdateDateOfBirthWhenNullInRequest() {
            // Given
            updateRequest.setDateOfBirth(null);
            LocalDate originalDateOfBirth = testTrainee.getDateOfBirth();

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.updateProfile("john.doe", updateRequest);

            // Then
            verify(traineeRepository).save(traineeCaptor.capture());
            assertThat(traineeCaptor.getValue().getDateOfBirth()).isEqualTo(originalDateOfBirth);
        }

        @Test
        @DisplayName("Should not update address when null in request")
        void shouldNotUpdateAddressWhenNullInRequest() {
            // Given
            updateRequest.setAddress(null);
            String originalAddress = testTrainee.getAddress();

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.updateProfile("john.doe", updateRequest);

            // Then
            verify(traineeRepository).save(traineeCaptor.capture());
            assertThat(traineeCaptor.getValue().getAddress()).isEqualTo(originalAddress);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("john.doe", "wrongpassword");
            updateRequest.setPassword("wrongpassword");

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile("john.doe", updateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(traineeRepository, never()).findByUser_Username(anyString());
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails on update")
        void shouldThrowValidationExceptionWhenValidationFailsOnUpdate() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid date of birth");
            Set<ConstraintViolation<Trainee>> violations = Set.of(violation);

            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile("john.doe", updateRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("Invalid date of birth");

            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteByUsername Tests")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Should delete trainee successfully")
        void shouldDeleteTraineeSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            traineeService.deleteByUsername("john.doe", "password123");

            // Then
            verify(userService).authenticate("john.doe", "password123");
            verify(traineeRepository).findByUser_Username("john.doe");
            verify(traineeRepository).delete(testTrainee);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails on delete")
        void shouldThrowExceptionWhenAuthenticationFailsOnDelete() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("john.doe", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> traineeService.deleteByUsername("john.doe", "wrongpassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(traineeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found on delete")
        void shouldThrowNotFoundExceptionWhenTraineeNotFoundOnDelete() {
            // Given
            doNothing().when(userService).authenticate("nonexistent", "password123");
            when(traineeRepository.findByUser_Username("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.deleteByUsername("nonexistent", "password123"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: nonexistent");

            verify(traineeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateTrainersList Tests")
    class UpdateTrainersListTests {

        private List<Trainer> trainers;

        @BeforeEach
        void setUp() {
            User trainerUser1 = User.builder()
                    .id(10L)
                    .firstName("Trainer")
                    .lastName("One")
                    .username("trainer1")
                    .build();

            User trainerUser2 = User.builder()
                    .id(11L)
                    .firstName("Trainer")
                    .lastName("Two")
                    .username("trainer2")
                    .build();

            TrainingType cardioType = new TrainingType(1L, TrainingTypeName.CARDIO);
            TrainingType strengthType = new TrainingType(2L, TrainingTypeName.STRENGTH);

            Trainer trainer1 = Trainer.builder()
                    .id(10L)
                    .user(trainerUser1)
                    .specialization(cardioType)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(11L)
                    .user(trainerUser2)
                    .specialization(strengthType)
                    .build();

            trainers = List.of(trainer1, trainer2);
        }

        @Test
        @DisplayName("Should update trainers list successfully")
        void shouldUpdateTrainersListSuccessfully() {
            // Given
            List<String> trainerUsernames = List.of("trainer1", "trainer2");

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(
                    "john.doe", "password123", trainerUsernames);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer2");

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).hasSize(2);
        }

        @Test
        @DisplayName("Should clear existing trainers and add new ones")
        void shouldClearExistingTrainersAndAddNewOnes() {
            // Given
            User existingTrainerUser = User.builder()
                    .id(99L)
                    .username("existing.trainer")
                    .build();
            Trainer existingTrainer = Trainer.builder()
                    .id(99L)
                    .user(existingTrainerUser)
                    .build();

            testTrainee.getTrainers().add(existingTrainer);

            List<String> newTrainerUsernames = List.of("trainer1");
            List<Trainer> newTrainers = List.of(trainers.getFirst());

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(newTrainerUsernames))
                    .thenReturn(newTrainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(
                    "john.doe", "password123", newTrainerUsernames);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getUsername()).isEqualTo("trainer1");

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getTrainers()).hasSize(1);
            assertThat(capturedTrainee.getTrainers().getFirst().getUser().getUsername())
                    .isEqualTo("trainer1");
        }

        @Test
        @DisplayName("Should return empty list when trainer usernames is null")
        void shouldReturnEmptyListWhenTrainerUsernamesIsNull() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(
                    "john.doe", "password123", null);

            // Then
            assertThat(result).isEmpty();
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("Should return empty list when trainer usernames is empty")
        void shouldReturnEmptyListWhenTrainerUsernamesIsEmpty() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(
                    "john.doe", "password123", Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
            verifyNoInteractions(trainerRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException when some trainers not found")
        void shouldThrowNotFoundExceptionWhenSomeTrainersNotFound() {
            // Given
            List<String> trainerUsernames = List.of("trainer1", "nonexistent");
            List<Trainer> foundTrainers = List.of(trainers.getFirst());

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(foundTrainers);

            // When & Then
            assertThatThrownBy(() -> traineeService.updateTrainersList(
                    "john.doe", "password123", trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("nonexistent");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when all trainers not found")
        void shouldThrowNotFoundExceptionWhenAllTrainersNotFound() {
            // Given
            List<String> trainerUsernames = List.of("nonexistent1", "nonexistent2");

            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> traineeService.updateTrainersList(
                    "john.doe", "password123", trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("nonexistent1")
                    .hasMessageContaining("nonexistent2");
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFailsOnUpdateTrainers() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("john.doe", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> traineeService.updateTrainersList(
                    "john.doe", "wrongpassword", List.of("trainer1")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verifyNoInteractions(traineeRepository);
            verifyNoInteractions(trainerRepository);
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate trainee successfully")
        void shouldAuthenticateTraineeSuccessfully() {
            // Given
            doNothing().when(userService).authenticate("john.doe", "password123");
            when(traineeRepository.findByUser_Username("john.doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            traineeService.authenticate("john.doe", "password123");

            // Then
            verify(userService).authenticate("john.doe", "password123");
            verify(traineeRepository).findByUser_Username("john.doe");
        }

        @Test
        @DisplayName("Should throw exception when user authentication fails")
        void shouldThrowExceptionWhenUserAuthenticationFails() {
            // Given
            doThrow(new RuntimeException("Invalid credentials"))
                    .when(userService).authenticate("john.doe", "wrongpassword");

            // When & Then
            assertThatThrownBy(() -> traineeService.authenticate("john.doe", "wrongpassword"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(traineeRepository, never()).findByUser_Username(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user exists but not a trainee")
        void shouldThrowNotFoundExceptionWhenUserExistsButNotATrainee() {
            // Given
            doNothing().when(userService).authenticate("trainer.user", "password123");
            when(traineeRepository.findByUser_Username("trainer.user"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.authenticate("trainer.user", "password123"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: trainer.user");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should handle multiple validation errors")
        void shouldHandleMultipleValidationErrors() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");

            when(userService.createUser("John", "Doe")).thenReturn(testUser);

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

            // When & Then
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(traineeRepository, never()).save(any());
        }
    }
}