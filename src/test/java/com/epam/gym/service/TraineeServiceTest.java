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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private Trainer testTrainer1;
    private Trainer testTrainer2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
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

        User trainerUser1 = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Trainer")
                .username("Mike.Trainer")
                .password("pass123")
                .isActive(true)
                .build();

        User trainerUser2 = User.builder()
                .id(3L)
                .firstName("Jane")
                .lastName("Coach")
                .username("Jane.Coach")
                .password("pass456")
                .isActive(true)
                .build();

        TrainingType fitness = TrainingType.builder()
                .id(1L)
                .trainingTypeName(TrainingTypeName.FITNESS)
                .build();

        testTrainer1 = Trainer.builder()
                .id(1L)
                .user(trainerUser1)
                .specialization(fitness)
                .build();

        testTrainer2 = Trainer.builder()
                .id(2L)
                .user(trainerUser2)
                .specialization(fitness)
                .build();
    }

    @Nested
    @DisplayName("createProfile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainee profile successfully")
        void createProfile_Success() {
            // Given
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .address("123 Main St")
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser().getUsername()).isEqualTo("John.Doe");
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(result.getAddress()).isEqualTo("123 Main St");

            verify(userService).createUser("John", "Doe");
            verify(validator).validate(any(Trainee.class));
            verify(traineeRepository).save(traineeCaptor.capture());

            Trainee capturedTrainee = traineeCaptor.getValue();
            assertThat(capturedTrainee.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainee.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should create trainee with null optional fields")
        void createProfile_WithNullOptionalFields() {
            // Given
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(null)
                    .address(null)
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDateOfBirth()).isNull();
            assertThat(result.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails() {
            // Given
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User must not be null");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User must not be null");

            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByUsername Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainee when found")
        void getByUsername_Success() {
            // Given
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            Trainee result = traineeService.getByUsername("John.Doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo("John.Doe");
            verify(traineeRepository).findByUser_Username("John.Doe");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getByUsername_NotFound() {
            // Given
            when(traineeRepository.findByUser_Username("Unknown.User"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.getByUsername("Unknown.User"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainee not found: Unknown.User");
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainee profile successfully")
        void updateProfile_Success() {
            // Given
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .firstName("Johnny")
                    .lastName("Updated")
                    .dateOfBirth(LocalDate.of(1991, 6, 20))
                    .address("456 New St")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            Trainee result = traineeService.updateProfile("John.Doe", request);

            // Then
            assertThat(result).isNotNull();
            verify(userService).isAuthenticated("John.Doe");
            verify(userService).updateUserBasicInfo(testUser, "Johnny", "Updated", true);
            verify(traineeRepository).save(traineeCaptor.capture());

            Trainee captured = traineeCaptor.getValue();
            assertThat(captured.getDateOfBirth()).isEqualTo(LocalDate.of(1991, 6, 20));
            assertThat(captured.getAddress()).isEqualTo("456 New St");
        }

        @Test
        @DisplayName("Should update only non-null fields")
        void updateProfile_PartialUpdate() {
            // Given
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .firstName("Johnny")
                    .lastName("Updated")
                    .dateOfBirth(null)  // Keep original
                    .address(null)       // Keep original
                    .isActive(true)
                    .build();

            LocalDate originalDob = testTrainee.getDateOfBirth();
            String originalAddress = testTrainee.getAddress();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.updateProfile("John.Doe", request);

            // Then
            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee captured = traineeCaptor.getValue();
            assertThat(captured.getDateOfBirth()).isEqualTo(originalDob);
            assertThat(captured.getAddress()).isEqualTo(originalAddress);
        }

        @Test
        @DisplayName("Should throw ValidationException when update validation fails")
        void updateProfile_ValidationFails() {
            // Given
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .firstName("")
                    .lastName("")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("First name is required");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile("John.Doe", request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("First name is required");

            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteByUsername Tests")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Should delete trainee successfully")
        void deleteByUsername_Success() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            traineeService.deleteByUsername("John.Doe");

            // Then
            verify(userService).isAuthenticated("John.Doe");
            verify(traineeRepository).findByUser_Username("John.Doe");
            verify(traineeRepository).delete(testTrainee);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent trainee")
        void deleteByUsername_NotFound() {
            // Given
            doNothing().when(userService).isAuthenticated("Unknown.User");
            when(traineeRepository.findByUser_Username("Unknown.User"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.deleteByUsername("Unknown.User"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainee not found: Unknown.User");

            verify(traineeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateTrainersList Tests")
    class UpdateTrainersListTests {

        @Test
        @DisplayName("Should update trainers list successfully")
        void updateTrainersList_Success() {
            // Given
            List<String> trainerUsernames = List.of("Mike.Trainer", "Jane.Coach");
            List<Trainer> trainers = List.of(testTrainer1, testTrainer2);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(trainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList("John.Doe", trainerUsernames);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testTrainer1, testTrainer2);

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee captured = traineeCaptor.getValue();
            assertThat(captured.getTrainers()).hasSize(2);
        }

        @Test
        @DisplayName("Should clear trainers list when empty list provided")
        void updateTrainersList_EmptyList() {
            // Given
            testTrainee.getTrainers().add(testTrainer1);  // Has existing trainer
            List<String> trainerUsernames = Collections.emptyList();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList("John.Doe", trainerUsernames);

            // Then
            assertThat(result).isEmpty();
            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
            verify(traineeRepository).save(traineeCaptor.capture());
            assertThat(traineeCaptor.getValue().getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null trainers list")
        void updateTrainersList_NullList() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList("John.Doe", null);

            // Then
            assertThat(result).isEmpty();
            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
        }

        @Test
        @DisplayName("Should throw NotFoundException when some trainers not found")
        void updateTrainersList_TrainersNotFound() {
            // Given
            List<String> trainerUsernames = List.of("Mike.Trainer", "Unknown.Trainer");
            List<Trainer> foundTrainers = List.of(testTrainer1);  // Only one found

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(foundTrainers);

            // When & Then
            assertThatThrownBy(() -> traineeService.updateTrainersList("John.Doe", trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("Unknown.Trainer");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should replace existing trainers with new list")
        void updateTrainersList_ReplaceExisting() {
            // Given
            testTrainee.getTrainers().add(testTrainer1);  // Has trainer1
            List<String> newTrainerUsernames = List.of("Jane.Coach");  // Replace with trainer2
            List<Trainer> newTrainers = List.of(testTrainer2);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(newTrainerUsernames))
                    .thenReturn(newTrainers);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList("John.Doe", newTrainerUsernames);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(testTrainer2);

            verify(traineeRepository).save(traineeCaptor.capture());
            Trainee captured = traineeCaptor.getValue();
            assertThat(captured.getTrainers()).hasSize(1);
            assertThat(captured.getTrainers()).containsExactly(testTrainer2);
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("updateProfile should call isAuthenticated")
        void updateProfile_CallsIsAuthenticated() {
            // Given
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.updateProfile("John.Doe", request);

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("deleteByUsername should call isAuthenticated")
        void deleteByUsername_CallsIsAuthenticated() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));

            // When
            traineeService.deleteByUsername("John.Doe");

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("updateTrainersList should call isAuthenticated")
        void updateTrainersList_CallsIsAuthenticated() {
            // Given
            List<String> trainerUsernames = List.of("Mike.Trainer");

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(List.of(testTrainer1));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);

            // When
            traineeService.updateTrainersList("John.Doe", trainerUsernames);

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle trainee with maximum length address")
        void createProfile_MaxLengthAddress() {
            // Given
            String longAddress = "A".repeat(500);
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .address(longAddress)
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            verify(traineeRepository).save(traineeCaptor.capture());
            assertThat(traineeCaptor.getValue().getAddress()).isEqualTo(longAddress);
        }

        @Test
        @DisplayName("Should handle date of birth in the past")
        void createProfile_PastDateOfBirth() {
            // Given
            LocalDate oldDate = LocalDate.of(1950, 1, 1);
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(oldDate)
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });

            // When
            Trainee result = traineeService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            verify(traineeRepository).save(traineeCaptor.capture());
            assertThat(traineeCaptor.getValue().getDateOfBirth()).isEqualTo(oldDate);
        }

        @Test
        @DisplayName("Should handle multiple validation errors")
        void createProfile_MultipleValidationErrors() {
            // Given
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userService.createUser("John", "Doe")).thenReturn(testUser);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation1 = org.mockito.Mockito.mock(ConstraintViolation.class);
            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation2 = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Error 1");
            when(violation2.getMessage()).thenReturn("Error 2");

            Set<ConstraintViolation<Trainee>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            when(validator.validate(any(Trainee.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");
        }

        @Test
        @DisplayName("Should handle duplicate trainer usernames in list")
        void updateTrainersList_DuplicateUsernames() {
            // Given
            List<String> trainerUsernames = List.of("Mike.Trainer", "Mike.Trainer");
            List<Trainer> trainers = List.of(testTrainer1);  // Repository returns unique

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(trainers);

            // When & Then - Should throw because size mismatch
            assertThatThrownBy(() -> traineeService.updateTrainersList("John.Doe", trainerUsernames))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}