package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.repository.UserRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private Validator validator;

    @Captor
    private ArgumentCaptor<Trainer> trainerCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(
                trainerRepository,
                traineeRepository,
                trainingTypeRepository,
                userRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Nested
    @DisplayName("createProfile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainer profile successfully")
        void shouldCreateTrainerProfileSuccessfully() {
            // Given
            String firstName = "John";
            String lastName = "Doe";
            TrainingTypeName specialization = TrainingTypeName.CARDIO;
            String generatedPassword = "password123";
            String generatedUsername = "john.doe";

            User savedUser = User.builder()
                    .id(1L)
                    .firstName(firstName)
                    .lastName(lastName)
                    .username(generatedUsername)
                    .password(generatedPassword)
                    .isActive(true)
                    .build();

            TrainingType trainingType = new TrainingType(1L, TrainingTypeName.CARDIO);

            Trainer savedTrainer = Trainer.builder()
                    .id(1L)
                    .user(savedUser)
                    .specialization(trainingType)
                    .build();

            when(passwordGenerator.generatePassword()).thenReturn(generatedPassword);
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(generatedUsername);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(trainingTypeRepository.findByTrainingTypeName(specialization))
                    .thenReturn(Optional.of(trainingType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(savedTrainer);

            // When
            Trainer result = trainerService.createProfile(firstName, lastName, specialization);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser().getUsername()).isEqualTo(generatedUsername);
            assertThat(result.getSpecialization()).isEqualTo(trainingType);

            verify(passwordGenerator).generatePassword();
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(userCaptor.capture());
            verify(trainingTypeRepository).findByTrainingTypeName(specialization);
            verify(trainerRepository).save(trainerCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getFirstName()).isEqualTo(firstName);
            assertThat(capturedUser.getLastName()).isEqualTo(lastName);
            assertThat(capturedUser.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void shouldThrowNotFoundExceptionWhenTrainingTypeNotFound() {
            // Given
            String firstName = "John";
            String lastName = "Doe";
            TrainingTypeName specialization = TrainingTypeName.YOGA;

            User savedUser = User.builder()
                    .id(1L)
                    .firstName(firstName)
                    .lastName(lastName)
                    .build();

            when(passwordGenerator.generatePassword()).thenReturn("password123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn("john.doe");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(trainingTypeRepository.findByTrainingTypeName(specialization))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(firstName, lastName, specialization))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found: " + specialization);

            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("selectByUsername Tests")
    class SelectByUsernameTests {

        @Test
        @DisplayName("Should return trainer when found")
        void shouldReturnTrainerWhenFound() {
            // Given
            String username = "john.doe";
            User user = User.builder()
                    .id(1L)
                    .username(username)
                    .build();

            Trainer trainer = Trainer.builder()
                    .id(1L)
                    .user(user)
                    .build();

            when(trainerRepository.findByUser_Username(username))
                    .thenReturn(Optional.of(trainer));

            // When
            Trainer result = trainerService.selectByUsername(username);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo(username);
            verify(trainerRepository).findByUser_Username(username);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void shouldThrowNotFoundExceptionWhenTrainerNotFound() {
            // Given
            String username = "nonexistent";
            when(trainerRepository.findByUser_Username(username))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.selectByUsername(username))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found: " + username);
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainer profile successfully")
        void shouldUpdateTrainerProfileSuccessfully() {
            // Given
            String username = "john.doe";
            String password = "password123";

            User existingUser = User.builder()
                    .id(1L)
                    .username(username)
                    .password(password)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            TrainingType existingSpecialization = new TrainingType(1L, TrainingTypeName.CARDIO);
            TrainingType newSpecialization = new TrainingType(2L, TrainingTypeName.YOGA);

            Trainer existingTrainer = Trainer.builder()
                    .id(1L)
                    .user(existingUser)
                    .specialization(existingSpecialization)
                    .build();

            User updatedUserData = User.builder()
                    .firstName("Johnny")
                    .lastName("Updated")
                    .build();

            Trainer updatedTrainerData = Trainer.builder()
                    .user(updatedUserData)
                    .specialization(newSpecialization)
                    .build();

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
            when(trainerRepository.findByUser_Username(username))
                    .thenReturn(Optional.of(existingTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);

            // When
            Trainer result = trainerService.updateProfile(username, password, updatedTrainerData);

            // Then
            assertThat(result).isNotNull();
            verify(trainerRepository).save(trainerCaptor.capture());

            Trainer captured = trainerCaptor.getValue();
            assertThat(captured.getUser().getFirstName()).isEqualTo("Johnny");
            assertThat(captured.getUser().getLastName()).isEqualTo("Updated");
            assertThat(captured.getSpecialization()).isEqualTo(newSpecialization);
        }

        @Test
        @DisplayName("Should update only first name when only first name provided")
        void shouldUpdateOnlyFirstName() {
            // Given
            String username = "john.doe";
            String password = "password123";

            User existingUser = User.builder()
                    .id(1L)
                    .username(username)
                    .password(password)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            Trainer existingTrainer = Trainer.builder()
                    .id(1L)
                    .user(existingUser)
                    .build();

            User updatedUserData = User.builder()
                    .firstName("Johnny")
                    .build();

            Trainer updatedTrainerData = Trainer.builder()
                    .user(updatedUserData)
                    .build();

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
            when(trainerRepository.findByUser_Username(username))
                    .thenReturn(Optional.of(existingTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);

            // When
            trainerService.updateProfile(username, password, updatedTrainerData);

            // Then
            verify(trainerRepository).save(trainerCaptor.capture());
            Trainer captured = trainerCaptor.getValue();
            assertThat(captured.getUser().getFirstName()).isEqualTo("Johnny");
            assertThat(captured.getUser().getLastName()).isEqualTo("Doe"); // Unchanged
        }
    }

    @Nested
    @DisplayName("getUnassignedTrainers Tests")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should return unassigned trainers")
        void shouldReturnUnassignedTrainers() {
            // Given
            String traineeUsername = "trainee.user";

            User traineeUser = User.builder()
                    .id(1L)
                    .username(traineeUsername)
                    .build();

            Trainee trainee = Trainee.builder()
                    .id(1L)
                    .user(traineeUser)
                    .build();

            Trainer trainer1 = Trainer.builder().id(1L).build();
            Trainer trainer2 = Trainer.builder().id(2L).build();
            List<Trainer> unassignedTrainers = List.of(trainer1, trainer2);

            when(traineeRepository.findByUser_Username(traineeUsername))
                    .thenReturn(Optional.of(trainee));
            when(trainerRepository.findAvailableTrainers(traineeUsername))
                    .thenReturn(unassignedTrainers);

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers(traineeUsername);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(trainer1, trainer2);
            verify(traineeRepository).findByUser_Username(traineeUsername);
            verify(trainerRepository).findAvailableTrainers(traineeUsername);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void shouldReturnEmptyListWhenNoUnassignedTrainers() {
            // Given
            String traineeUsername = "trainee.user";

            Trainee trainee = Trainee.builder().id(1L).build();

            when(traineeRepository.findByUser_Username(traineeUsername))
                    .thenReturn(Optional.of(trainee));
            when(trainerRepository.findAvailableTrainers(traineeUsername))
                    .thenReturn(Collections.emptyList());

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers(traineeUsername);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void shouldThrowNotFoundExceptionWhenTraineeNotFound() {
            // Given
            String traineeUsername = "nonexistent";
            when(traineeRepository.findByUser_Username(traineeUsername))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers(traineeUsername))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: " + traineeUsername);

            verify(trainerRepository, never()).findAvailableTrainers(anyString());
        }
    }

    @Nested
    @DisplayName("extractUser Tests")
    class ExtractUserTests {

        @Test
        @DisplayName("Should extract user from trainer")
        void shouldExtractUserFromTrainer() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .username("test.user")
                    .build();

            Trainer trainer = Trainer.builder()
                    .id(1L)
                    .user(user)
                    .build();

            // When - using findByUsername which internally uses extractUser
            when(trainerRepository.findByUser_Username("test.user"))
                    .thenReturn(Optional.of(trainer));

            Trainer result = trainerService.selectByUsername("test.user");

            // Then
            assertThat(result.getUser()).isEqualTo(user);
        }
    }
}