package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TraineeService Integration Tests")
class TraineeServiceIT {

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @BeforeEach
    void setUp() {
        traineeRepository.deleteAll();
        trainerRepository.deleteAll();
        userRepository.deleteAll();
        // Note: Don't delete training types - they are reference data initialized by DataInitializer
    }

    /**
     * Helper method to get existing TrainingType from database.
     * TrainingTypes are pre-populated by DataInitializer, so we fetch them instead of creating new ones.
     */
    private TrainingType getTrainingType(TrainingTypeName typeName) {
        return trainingTypeRepository.findByTrainingTypeName(typeName)
                .orElseThrow(() -> new RuntimeException("TrainingType not found: " + typeName));
    }

    @Nested
    @DisplayName("createProfile Integration Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainee profile and persist to database")
        void shouldCreateTraineeProfileAndPersistToDatabase() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setDateOfBirth(LocalDate.of(1990, 5, 15));
            request.setAddress("123 Main St");

            Trainee result = traineeService.createProfile(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo("John.Doe");
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(result.getAddress()).isEqualTo("123 Main St");

            Trainee persistedTrainee = traineeRepository.findById(result.getId()).orElseThrow();
            assertThat(persistedTrainee.getUser().getUsername()).isEqualTo("John.Doe");
        }

        @Test
        @DisplayName("Should create trainee without optional fields")
        void shouldCreateTraineeWithoutOptionalFields() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("Jane");
            request.setLastName("Smith");

            Trainee result = traineeService.createProfile(request);

            assertThat(result.getDateOfBirth()).isNull();
            assertThat(result.getAddress()).isNull();

            Trainee persisted = traineeRepository.findByUser_Username("Jane.Smith").orElseThrow();
            assertThat(persisted.getDateOfBirth()).isNull();
        }

        @Test
        @DisplayName("Should handle duplicate names by appending serial number")
        void shouldHandleDuplicateNamesByAppendingSerialNumber() {
            TraineeRegistrationRequest request1 = new TraineeRegistrationRequest();
            request1.setFirstName("John");
            request1.setLastName("Doe");
            traineeService.createProfile(request1);

            TraineeRegistrationRequest request2 = new TraineeRegistrationRequest();
            request2.setFirstName("John");
            request2.setLastName("Doe");
            Trainee result2 = traineeService.createProfile(request2);

            assertThat(result2.getUser().getUsername()).isEqualTo("John.Doe1");
            assertThat(traineeRepository.findByUser_Username("John.Doe")).isPresent();
            assertThat(traineeRepository.findByUser_Username("John.Doe1")).isPresent();
        }
    }

    @Nested
    @DisplayName("getByUsername Integration Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainee when exists in database")
        void shouldReturnTraineeWhenExistsInDatabase() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            Trainee created = traineeService.createProfile(request);

            Trainee result = traineeService.getByUsername("John.Doe");

            assertThat(result.getId()).isEqualTo(created.getId());
            assertThat(result.getUser().getUsername()).isEqualTo("John.Doe");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee does not exist")
        void shouldThrowNotFoundExceptionWhenTraineeDoesNotExist() {
            assertThatThrownBy(() -> traineeService.getByUsername("nonexistent.user"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: nonexistent.user");
        }
    }

    @Nested
    @DisplayName("updateProfile Integration Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainee profile and persist changes")
        void shouldUpdateTraineeProfileAndPersistChanges() {
            TraineeRegistrationRequest createRequest = new TraineeRegistrationRequest();
            createRequest.setFirstName("John");
            createRequest.setLastName("Doe");
            createRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
            createRequest.setAddress("123 Old St");
            Trainee created = traineeService.createProfile(createRequest);
            String password = created.getUser().getPassword();

            UpdateTraineeRequest updateRequest = new UpdateTraineeRequest();
            updateRequest.setPassword(password);
            updateRequest.setFirstName("Johnny");
            updateRequest.setLastName("Updated");
            updateRequest.setIsActive(false);
            updateRequest.setDateOfBirth(LocalDate.of(1991, 6, 20));
            updateRequest.setAddress("456 New St");

            Trainee result = traineeService.updateProfile("John.Doe", updateRequest);

            assertThat(result.getUser().getFirstName()).isEqualTo("Johnny");
            assertThat(result.getAddress()).isEqualTo("456 New St");

            Trainee persisted = traineeRepository.findByUser_Username("John.Doe").orElseThrow();
            assertThat(persisted.getAddress()).isEqualTo("456 New St");
        }

        @Test
        @DisplayName("Should not update fields when null in request")
        void shouldNotUpdateFieldsWhenNullInRequest() {
            TraineeRegistrationRequest createRequest = new TraineeRegistrationRequest();
            createRequest.setFirstName("John");
            createRequest.setLastName("Doe");
            createRequest.setDateOfBirth(LocalDate.of(1990, 5, 15));
            createRequest.setAddress("123 Main St");
            Trainee created = traineeService.createProfile(createRequest);
            String password = created.getUser().getPassword();

            UpdateTraineeRequest updateRequest = new UpdateTraineeRequest();
            updateRequest.setPassword(password);
            updateRequest.setFirstName("John");
            updateRequest.setLastName("Doe");
            updateRequest.setIsActive(true);
            updateRequest.setDateOfBirth(null);
            updateRequest.setAddress(null);

            Trainee result = traineeService.updateProfile("John.Doe", updateRequest);

            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(result.getAddress()).isEqualTo("123 Main St");
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void shouldThrowExceptionWhenPasswordIsIncorrect() {
            TraineeRegistrationRequest createRequest = new TraineeRegistrationRequest();
            createRequest.setFirstName("John");
            createRequest.setLastName("Doe");
            traineeService.createProfile(createRequest);

            UpdateTraineeRequest updateRequest = new UpdateTraineeRequest();
            updateRequest.setPassword("wrongpassword");
            updateRequest.setFirstName("John");
            updateRequest.setLastName("Doe");
            updateRequest.setIsActive(true);

            assertThatThrownBy(() -> traineeService.updateProfile("John.Doe", updateRequest))
                    .isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("deleteByUsername Integration Tests")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Should delete trainee and associated user from database")
        void shouldDeleteTraineeAndAssociatedUserFromDatabase() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            Trainee created = traineeService.createProfile(request);
            String password = created.getUser().getPassword();
            Long userId = created.getUser().getId();

            assertThat(traineeRepository.findByUser_Username("John.Doe")).isPresent();

            traineeService.deleteByUsername("John.Doe", password);

            assertThat(traineeRepository.findByUser_Username("John.Doe")).isEmpty();
            assertThat(userRepository.findById(userId)).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect on delete")
        void shouldThrowExceptionWhenPasswordIsIncorrectOnDelete() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            traineeService.createProfile(request);

            assertThatThrownBy(() -> traineeService.deleteByUsername("John.Doe", "wrongpassword"))
                    .isInstanceOf(AuthenticationException.class);

            assertThat(traineeRepository.findByUser_Username("John.Doe")).isPresent();
        }
    }

    @Nested
    @DisplayName("updateTrainersList Integration Tests")
    class UpdateTrainersListTests {

        @Test
        @DisplayName("Should update trainers list and persist many-to-many relationship")
        void shouldUpdateTrainersListAndPersistManyToManyRelationship() {
            // Create trainee
            TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
            traineeRequest.setFirstName("John");
            traineeRequest.setLastName("Doe");
            Trainee trainee = traineeService.createProfile(traineeRequest);
            String password = trainee.getUser().getPassword();

            // Get existing training type (already created by DataInitializer)
            TrainingType cardioType = getTrainingType(TrainingTypeName.CARDIO);

            // Create trainer 1
            User trainerUser1 = User.builder()
                    .firstName("Trainer")
                    .lastName("One")
                    .username("trainer1")
                    .password("pass123")
                    .isActive(true)
                    .build();
            userRepository.save(trainerUser1);

            Trainer trainer1 = Trainer.builder()
                    .user(trainerUser1)
                    .specialization(cardioType)
                    .build();
            trainerRepository.save(trainer1);

            // Create trainer 2
            User trainerUser2 = User.builder()
                    .firstName("Trainer")
                    .lastName("Two")
                    .username("trainer2")
                    .password("pass123")
                    .isActive(true)
                    .build();
            userRepository.save(trainerUser2);

            Trainer trainer2 = Trainer.builder()
                    .user(trainerUser2)
                    .specialization(cardioType)
                    .build();
            trainerRepository.save(trainer2);

            // Update trainers list
            List<Trainer> result = traineeService.updateTrainersList(
                    "John.Doe", password, List.of("trainer1", "trainer2"));

            assertThat(result).hasSize(2);

            Trainee persistedTrainee = traineeRepository.findByUser_Username("John.Doe").orElseThrow();
            assertThat(persistedTrainee.getTrainers()).hasSize(2);
        }

        @Test
        @DisplayName("Should replace existing trainers with new list")
        void shouldReplaceExistingTrainersWithNewList() {
            // Create trainee
            TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
            traineeRequest.setFirstName("John");
            traineeRequest.setLastName("Doe");
            Trainee trainee = traineeService.createProfile(traineeRequest);
            String password = trainee.getUser().getPassword();

            // Get existing training type
            TrainingType type = getTrainingType(TrainingTypeName.STRENGTH);

            // Create old trainer
            User trainerUser1 = User.builder()
                    .firstName("Trainer")
                    .lastName("Old")
                    .username("trainer.old")
                    .password("pass123")
                    .isActive(true)
                    .build();
            userRepository.save(trainerUser1);
            Trainer oldTrainer = Trainer.builder()
                    .user(trainerUser1)
                    .specialization(type)
                    .build();
            trainerRepository.save(oldTrainer);

            traineeService.updateTrainersList("John.Doe", password, List.of("trainer.old"));

            // Create new trainer
            User trainerUser2 = User.builder()
                    .firstName("Trainer")
                    .lastName("New")
                    .username("trainer.new")
                    .password("pass123")
                    .isActive(true)
                    .build();
            userRepository.save(trainerUser2);
            Trainer newTrainer = Trainer.builder()
                    .user(trainerUser2)
                    .specialization(type)
                    .build();
            trainerRepository.save(newTrainer);

            // Replace with new trainer
            List<Trainer> result = traineeService.updateTrainersList(
                    "John.Doe", password, List.of("trainer.new"));

            assertThat(result).hasSize(1);

            Trainee persisted = traineeRepository.findByUser_Username("John.Doe").orElseThrow();
            assertThat(persisted.getTrainers()).hasSize(1);
            assertThat(persisted.getTrainers().getFirst().getUser().getUsername()).isEqualTo("trainer.new");
        }

        @Test
        @DisplayName("Should clear all trainers when empty list provided")
        void shouldClearAllTrainersWhenEmptyListProvided() {
            // Create trainee
            TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
            traineeRequest.setFirstName("John");
            traineeRequest.setLastName("Doe");
            Trainee trainee = traineeService.createProfile(traineeRequest);
            String password = trainee.getUser().getPassword();

            // Get existing training type
            TrainingType type = getTrainingType(TrainingTypeName.CARDIO);

            // Create trainer
            User trainerUser = User.builder()
                    .firstName("Trainer")
                    .lastName("One")
                    .username("trainer1")
                    .password("pass123")
                    .isActive(true)
                    .build();
            userRepository.save(trainerUser);
            Trainer trainer = Trainer.builder()
                    .user(trainerUser)
                    .specialization(type)
                    .build();
            trainerRepository.save(trainer);

            traineeService.updateTrainersList("John.Doe", password, List.of("trainer1"));

            // Clear all trainers
            List<Trainer> result = traineeService.updateTrainersList(
                    "John.Doe", password, Collections.emptyList());

            assertThat(result).isEmpty();

            Trainee persisted = traineeRepository.findByUser_Username("John.Doe").orElseThrow();
            assertThat(persisted.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer does not exist")
        void shouldThrowNotFoundExceptionWhenTrainerDoesNotExist() {
            TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
            traineeRequest.setFirstName("John");
            traineeRequest.setLastName("Doe");
            Trainee trainee = traineeService.createProfile(traineeRequest);
            String password = trainee.getUser().getPassword();

            assertThatThrownBy(() -> traineeService.updateTrainersList(
                    "John.Doe", password, List.of("nonexistent.trainer")))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("nonexistent.trainer");
        }
    }

    @Nested
    @DisplayName("authenticate Integration Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate trainee with correct credentials")
        void shouldAuthenticateTraineeWithCorrectCredentials() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            Trainee created = traineeService.createProfile(request);
            String password = created.getUser().getPassword();

            // Should not throw any exception
            traineeService.authenticate("John.Doe", password);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrect() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            traineeService.createProfile(request);

            assertThatThrownBy(() -> traineeService.authenticate("John.Doe", "wrongpassword"))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when username does not exist")
        void shouldThrowAuthenticationExceptionWhenUsernameDoesNotExist() {
            // Based on your UserService implementation, authenticate throws AuthenticationException
            // for both invalid username and invalid password
            assertThatThrownBy(() -> traineeService.authenticate("nonexistent.user", "password"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid username or password");
        }
    }

    @Nested
    @DisplayName("End-to-End Workflow Integration Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full trainee lifecycle workflow")
        void shouldCompleteFullTraineeLifecycleWorkflow() {
            // Step 1: Create trainee
            TraineeRegistrationRequest createRequest = new TraineeRegistrationRequest();
            createRequest.setFirstName("Alice");
            createRequest.setLastName("Wonderland");
            createRequest.setDateOfBirth(LocalDate.of(1995, 3, 20));
            createRequest.setAddress("Wonderland Ave");

            Trainee created = traineeService.createProfile(createRequest);
            String password = created.getUser().getPassword();
            String username = created.getUser().getUsername();

            assertThat(username).isEqualTo("Alice.Wonderland");

            // Step 2: Get trainee
            Trainee retrieved = traineeService.getByUsername(username);
            assertThat(retrieved.getAddress()).isEqualTo("Wonderland Ave");

            // Step 3: Update trainee
            UpdateTraineeRequest updateRequest = new UpdateTraineeRequest();
            updateRequest.setPassword(password);
            updateRequest.setFirstName("Alice");
            updateRequest.setLastName("Updated");
            updateRequest.setIsActive(true);
            updateRequest.setDateOfBirth(LocalDate.of(1996, 4, 21));
            updateRequest.setAddress("Updated Address");

            Trainee updated = traineeService.updateProfile(username, updateRequest);
            assertThat(updated.getAddress()).isEqualTo("Updated Address");

            // Step 4: Authenticate
            traineeService.authenticate(username, password);

            // Step 5: Delete trainee
            traineeService.deleteByUsername(username, password);

            // Verify deletion
            assertThatThrownBy(() -> traineeService.getByUsername(username))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should manage trainers for trainee throughout lifecycle")
        void shouldManageTrainersForTraineeThroughoutLifecycle() {
            // Create trainee
            TraineeRegistrationRequest traineeRequest = new TraineeRegistrationRequest();
            traineeRequest.setFirstName("Bob");
            traineeRequest.setLastName("Builder");
            Trainee trainee = traineeService.createProfile(traineeRequest);
            String password = trainee.getUser().getPassword();
            String username = trainee.getUser().getUsername();

            // Get existing training type
            TrainingType type = getTrainingType(TrainingTypeName.YOGA);

            // Create multiple trainers
            for (int i = 1; i <= 3; i++) {
                User trainerUser = User.builder()
                        .firstName("Trainer")
                        .lastName("Number" + i)
                        .username("trainer" + i)
                        .password("pass123")
                        .isActive(true)
                        .build();
                userRepository.save(trainerUser);

                Trainer trainer = Trainer.builder()
                        .user(trainerUser)
                        .specialization(type)
                        .build();
                trainerRepository.save(trainer);
            }

            // Add first two trainers
            List<Trainer> trainers = traineeService.updateTrainersList(
                    username, password, List.of("trainer1", "trainer2"));
            assertThat(trainers).hasSize(2);

            // Replace with all three trainers
            trainers = traineeService.updateTrainersList(
                    username, password, List.of("trainer1", "trainer2", "trainer3"));
            assertThat(trainers).hasSize(3);

            // Remove all trainers
            trainers = traineeService.updateTrainersList(
                    username, password, Collections.emptyList());
            assertThat(trainers).isEmpty();

            // Verify in database
            Trainee persisted = traineeRepository.findByUser_Username(username).orElseThrow();
            assertThat(persisted.getTrainers()).isEmpty();
        }
    }
}