package com.epam.gym.repository;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TrainerRepository Tests")
class TrainerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainerRepository trainerRepository;

    private TrainingType cardioType;
    private TrainingType strengthType;

    @BeforeEach
    void setUp() {
        cardioType = createTrainingType(TrainingTypeName.CARDIO);
        strengthType = createTrainingType(TrainingTypeName.STRENGTH);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByUser_Username Tests")
    class FindByUserUsernameTests {

        @Test
        @DisplayName("Should find trainer by username")
        void shouldFindTrainerByUsername() {
            // Given
            User user = createUser("john.doe", "John", "Doe", true);
            createTrainer(user, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Trainer> result = trainerRepository.findByUser_Username("john.doe");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUser().getUsername()).isEqualTo("john.doe");
            assertThat(result.get().getUser().getFirstName()).isEqualTo("John");
            assertThat(result.get().getSpecialization()).isNotNull();
        }

        @Test
        @DisplayName("Should return empty when trainer not found by username")
        void shouldReturnEmptyWhenTrainerNotFoundByUsername() {
            // When
            Optional<Trainer> result = trainerRepository.findByUser_Username("nonexistent");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find trainer with case-sensitive username")
        void shouldFindTrainerWithCaseSensitiveUsername() {
            // Given
            User user = createUser("John.Doe", "John", "Doe", true);
            createTrainer(user, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Trainer> resultExact = trainerRepository.findByUser_Username("John.Doe");
            Optional<Trainer> resultLower = trainerRepository.findByUser_Username("john.doe");

            // Then
            assertThat(resultExact).isPresent();
            assertThat(resultLower).isEmpty();
        }

        @Test
        @DisplayName("Should eagerly load user and specialization")
        void shouldEagerlyLoadUserAndSpecialization() {
            // Given
            User user = createUser("jane.trainer", "Jane", "Trainer", true);
            createTrainer(user, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Trainer> result = trainerRepository.findByUser_Username("jane.trainer");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getUser().getFirstName()).isEqualTo("Jane");
            assertThat(result.get().getUser().getLastName()).isEqualTo("Trainer");
            assertThat(result.get().getSpecialization()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByUser_UsernameIn Tests")
    class FindByUserUsernameInTests {

        @Test
        @DisplayName("Should find trainers by list of usernames")
        void shouldFindTrainersByListOfUsernames() {
            // Given
            User user1 = createUser("trainer1", "John", "Doe", true);
            User user2 = createUser("trainer2", "Jane", "Smith", true);
            User user3 = createUser("trainer3", "Bob", "Wilson", true);

            createTrainer(user1, cardioType);
            createTrainer(user2, strengthType);
            createTrainer(user3, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                    List.of("trainer1", "trainer2"));

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer2");
        }

        @Test
        @DisplayName("Should return empty list when no usernames match")
        void shouldReturnEmptyListWhenNoUsernamesMatch() {
            // Given
            User user = createUser("trainer1", "John", "Doe", true);
            createTrainer(user, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                    List.of("nonexistent1", "nonexistent2"));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return partial results when some usernames match")
        void shouldReturnPartialResultsWhenSomeUsernamesMatch() {
            // Given
            User user1 = createUser("trainer1", "John", "Doe", true);
            createTrainer(user1, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                    List.of("trainer1", "nonexistent"));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getUsername()).isEqualTo("trainer1");
        }

        @Test
        @DisplayName("Should handle empty username list")
        void shouldHandleEmptyUsernameList() {
            // Given
            User user = createUser("trainer1", "John", "Doe", true);
            createTrainer(user, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(List.of());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should eagerly load user and specialization for all trainers")
        void shouldEagerlyLoadUserAndSpecializationForAllTrainers() {
            // Given
            User user1 = createUser("trainer1", "John", "Doe", true);
            User user2 = createUser("trainer2", "Jane", "Smith", true);

            createTrainer(user1, cardioType);
            createTrainer(user2, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                    List.of("trainer1", "trainer2"));

            // Then
            assertThat(result).hasSize(2);
            result.forEach(trainer -> {
                assertThat(trainer.getUser().getFirstName()).isNotNull();
                assertThat(trainer.getSpecialization()).isNotNull();
            });
        }

        @Test
        @DisplayName("Should find single trainer when list contains one username")
        void shouldFindSingleTrainerWhenListContainsOneUsername() {
            // Given
            User user = createUser("single.trainer", "Single", "Trainer", true);
            createTrainer(user, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findByUser_UsernameIn(
                    List.of("single.trainer"));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getUsername()).isEqualTo("single.trainer");
        }
    }

    @Nested
    @DisplayName("findAvailableTrainers Tests")
    class FindAvailableTrainersTests {

        @Test
        @DisplayName("Should find all active trainers when trainee has no assigned trainers")
        void shouldFindAllActiveTrainersWhenTraineeHasNoAssignedTrainers() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            createTrainee(traineeUser);

            User trainerUser1 = createUser("trainer1", "Trainer", "One", true);
            User trainerUser2 = createUser("trainer2", "Trainer", "Two", true);

            createTrainer(trainerUser1, cardioType);
            createTrainer(trainerUser2, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer2");
        }

        @Test
        @DisplayName("Should exclude assigned trainers from available list")
        void shouldExcludeAssignedTrainersFromAvailableList() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            Trainee trainee = createTrainee(traineeUser);

            User trainerUser1 = createUser("trainer1", "Trainer", "One", true);
            User trainerUser2 = createUser("trainer2", "Trainer", "Two", true);
            User trainerUser3 = createUser("trainer3", "Trainer", "Three", true);

            Trainer trainer1 = createTrainer(trainerUser1, cardioType);
            createTrainer(trainerUser2, strengthType);
            Trainer trainer3 = createTrainer(trainerUser3, cardioType);

            trainee.getTrainers().add(trainer1);
            trainee.getTrainers().add(trainer3);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getUsername()).isEqualTo("trainer2");
        }

        @Test
        @DisplayName("Should exclude inactive trainers from available list")
        void shouldExcludeInactiveTrainersFromAvailableList() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            createTrainee(traineeUser);

            User activeTrainerUser = createUser("active.trainer", "Active", "Trainer", true);
            User inactiveTrainerUser = createUser("inactive.trainer", "Inactive", "Trainer", false);

            createTrainer(activeTrainerUser, cardioType);
            createTrainer(inactiveTrainerUser, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getUsername()).isEqualTo("active.trainer");
        }

        @Test
        @DisplayName("Should return empty list when all trainers are assigned")
        void shouldReturnEmptyListWhenAllTrainersAreAssigned() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            Trainee trainee = createTrainee(traineeUser);

            User trainerUser1 = createUser("trainer1", "Trainer", "One", true);
            User trainerUser2 = createUser("trainer2", "Trainer", "Two", true);

            Trainer trainer1 = createTrainer(trainerUser1, cardioType);
            Trainer trainer2 = createTrainer(trainerUser2, strengthType);

            trainee.getTrainers().add(trainer1);
            trainee.getTrainers().add(trainer2);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when all trainers are inactive")
        void shouldReturnEmptyListWhenAllTrainersAreInactive() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            createTrainee(traineeUser);

            User inactiveTrainer1 = createUser("inactive1", "Inactive", "One", false);
            User inactiveTrainer2 = createUser("inactive2", "Inactive", "Two", false);

            createTrainer(inactiveTrainer1, cardioType);
            createTrainer(inactiveTrainer2, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should eagerly load user and specialization for available trainers")
        void shouldEagerlyLoadUserAndSpecializationForAvailableTrainers() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            createTrainee(traineeUser);

            User trainerUser = createUser("trainer1", "Trainer", "One", true);
            createTrainer(trainerUser, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getFirstName()).isEqualTo("Trainer");
            assertThat(result.getFirst().getSpecialization()).isNotNull();
        }

        @Test
        @DisplayName("Should handle trainee with empty trainers list")
        void shouldHandleTraineeWithEmptyTrainersList() {
            // Given
            User traineeUser = createUser("trainee1", "Trainee", "One", true);
            createTrainee(traineeUser);

            User trainerUser = createUser("trainer1", "Trainer", "One", true);
            createTrainer(trainerUser, cardioType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee1");

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return all active trainers for nonexistent trainee")
        void shouldReturnAllActiveTrainersForNonexistentTrainee() {
            // Given
            User trainerUser1 = createUser("trainer1", "Trainer", "One", true);
            User trainerUser2 = createUser("trainer2", "Trainer", "Two", true);

            createTrainer(trainerUser1, cardioType);
            createTrainer(trainerUser2, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAvailableTrainers("nonexistent.trainee");

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle multiple trainees with different assigned trainers")
        void shouldHandleMultipleTraineesWithDifferentAssignedTrainers() {
            // Given
            User traineeUser1 = createUser("trainee1", "Trainee", "One", true);
            User traineeUser2 = createUser("trainee2", "Trainee", "Two", true);
            Trainee trainee1 = createTrainee(traineeUser1);
            Trainee trainee2 = createTrainee(traineeUser2);

            User trainerUser1 = createUser("trainer1", "Trainer", "One", true);
            User trainerUser2 = createUser("trainer2", "Trainer", "Two", true);
            User trainerUser3 = createUser("trainer3", "Trainer", "Three", true);

            Trainer trainer1 = createTrainer(trainerUser1, cardioType);
            Trainer trainer2 = createTrainer(trainerUser2, strengthType);
            createTrainer(trainerUser3, cardioType);

            trainee1.getTrainers().add(trainer1);
            trainee2.getTrainers().add(trainer2);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> resultForTrainee1 = trainerRepository.findAvailableTrainers("trainee1");
            List<Trainer> resultForTrainee2 = trainerRepository.findAvailableTrainers("trainee2");

            // Then
            assertThat(resultForTrainee1).hasSize(2);
            assertThat(resultForTrainee1)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer2", "trainer3");

            assertThat(resultForTrainee2).hasSize(2);
            assertThat(resultForTrainee2)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer3");
        }
    }

    @Nested
    @DisplayName("JpaRepository Standard Methods Tests")
    class JpaRepositoryStandardMethodsTests {

        @Test
        @DisplayName("Should save and retrieve trainer")
        void shouldSaveAndRetrieveTrainer() {
            // Given
            User user = createUser("new.trainer", "New", "Trainer", true);
            Trainer trainer = Trainer.builder()
                    .user(user)
                    .specialization(cardioType)
                    .build();

            // When
            Trainer savedTrainer = trainerRepository.save(trainer);
            entityManager.flush();
            entityManager.clear();

            Optional<Trainer> retrieved = trainerRepository.findById(savedTrainer.getId());

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getUser().getUsername()).isEqualTo("new.trainer");
        }

        @Test
        @DisplayName("Should find all trainers")
        void shouldFindAllTrainers() {
            // Given
            User user1 = createUser("trainer1", "John", "Doe", true);
            User user2 = createUser("trainer2", "Jane", "Smith", true);

            createTrainer(user1, cardioType);
            createTrainer(user2, strengthType);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Trainer> result = trainerRepository.findAll();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete trainer")
        void shouldDeleteTrainer() {
            // Given
            User user = createUser("to.delete", "Delete", "Me", true);
            Trainer trainer = createTrainer(user, cardioType);
            entityManager.flush();
            Long trainerId = trainer.getId();

            // When
            trainerRepository.deleteById(trainerId);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<Trainer> result = trainerRepository.findById(trainerId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if trainer exists by id")
        void shouldCheckIfTrainerExistsById() {
            // Given
            User user = createUser("exists.trainer", "Exists", "Trainer", true);
            Trainer trainer = createTrainer(user, cardioType);
            entityManager.flush();

            // When & Then
            assertThat(trainerRepository.existsById(trainer.getId())).isTrue();
            assertThat(trainerRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should count trainers")
        void shouldCountTrainers() {
            // Given
            User user1 = createUser("trainer1", "John", "Doe", true);
            User user2 = createUser("trainer2", "Jane", "Smith", true);

            createTrainer(user1, cardioType);
            createTrainer(user2, strengthType);
            entityManager.flush();

            // When
            long count = trainerRepository.count();

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    // Helper methods for creating test entities

    private TrainingType createTrainingType(TrainingTypeName name) {
        TrainingType trainingType = new TrainingType(null, name);
        return entityManager.persist(trainingType);
    }

    private User createUser(String username, String firstName, String lastName, boolean isActive) {
        User user = User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password("password123")
                .isActive(isActive)
                .build();
        return entityManager.persist(user);
    }

    private Trainer createTrainer(User user, TrainingType specialization) {
        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
        return entityManager.persist(trainer);
    }

    private Trainee createTrainee(User user) {
        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Test Address")
                .build();
        return entityManager.persist(trainee);
    }
}