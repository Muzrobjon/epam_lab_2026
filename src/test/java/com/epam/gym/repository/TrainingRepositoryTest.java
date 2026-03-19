package com.epam.gym.repository;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
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
@DisplayName("TrainingRepository Tests")
class TrainingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TrainingRepository trainingRepository;

    private TrainingType cardioType;
    private TrainingType strengthType;
    private Trainee trainee1;
    private Trainee trainee2;
    private Trainer trainer1;
    private Trainer trainer2;

    @BeforeEach
    void setUp() {
        cardioType = createTrainingType(TrainingTypeName.CARDIO);
        strengthType = createTrainingType(TrainingTypeName.STRENGTH);

        User traineeUser1 = createUser("trainee1", "John", "Doe");
        User traineeUser2 = createUser("trainee2", "Jane", "Smith");
        trainee1 = createTrainee(traineeUser1);
        trainee2 = createTrainee(traineeUser2);

        User trainerUser1 = createUser("trainer1", "Mike", "Johnson");
        User trainerUser2 = createUser("trainer2", "Sarah", "Wilson");
        trainer1 = createTrainer(trainerUser1, cardioType);
        trainer2 = createTrainer(trainerUser2, strengthType);

        entityManager.flush();
    }

    @Nested
    @DisplayName("findTrainingsWithAllUsers Tests")
    class FindTrainingsWithAllUsersTests {

        @Test
        @DisplayName("Should find all trainings when no filters applied")
        void shouldFindAllTrainingsWhenNoFiltersApplied() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, null, null);

            // Then
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should filter by trainee username")
        void shouldFilterByTraineeUsername() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", null, null, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainee().getUser().getUsername())
                    .containsOnly("trainee1");
        }

        @Test
        @DisplayName("Should filter by trainer username")
        void shouldFilterByTrainerUsername() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, "trainer2", null, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainer().getUser().getUsername())
                    .containsOnly("trainer2");
        }

        @Test
        @DisplayName("Should filter by both trainee and trainer username")
        void shouldFilterByBothTraineeAndTrainerUsername() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", "trainer2", null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainee().getUser().getUsername()).isEqualTo("trainee1");
            assertThat(result.getFirst().getTrainer().getUser().getUsername()).isEqualTo("trainer2");
        }

        @Test
        @DisplayName("Should filter by fromDate")
        void shouldFilterByFromDate() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 5, 1), 60);
            createTraining("Training 2", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, LocalDate.of(2024, 6, 1), null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Training::getTrainingDate)
                    .allMatch(date -> !date.isBefore(LocalDate.of(2024, 6, 1)));
        }

        @Test
        @DisplayName("Should filter by toDate")
        void shouldFilterByToDate() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 5, 1), 60);
            createTraining("Training 2", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, null, LocalDate.of(2024, 6, 30));

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Training::getTrainingDate)
                    .allMatch(date -> !date.isAfter(LocalDate.of(2024, 6, 30)));
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 5, 1), 60);
            createTraining("Training 2", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 7, 1), 30);
            createTraining("Training 4", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 8, 1), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 15));

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Training::getTrainingDate)
                    .containsExactlyInAnyOrder(
                            LocalDate.of(2024, 6, 15),
                            LocalDate.of(2024, 7, 1)
                    );
        }

        @Test
        @DisplayName("Should filter by all criteria")
        void shouldFilterByAllCriteria() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 6, 20), 30);
            createTraining("Training 4", trainee2, trainer1, cardioType,
                    LocalDate.of(2024, 6, 25), 30);
            createTraining("Training 5", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 7, 15), 30);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", "trainer1",
                    LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 30));

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingName()).isEqualTo("Training 2");
        }

        @Test
        @DisplayName("Should return empty list when no trainings match")
        void shouldReturnEmptyListWhenNoTrainingsMatch() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "nonexistent", null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when date range has no trainings")
        void shouldReturnEmptyListWhenDateRangeHasNoTrainings() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should eagerly load all related entities")
        void shouldEagerlyLoadAllRelatedEntities() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, null, null);

            // Then
            assertThat(result).hasSize(1);
            Training training = result.getFirst();

            // Verify eager loading - no lazy initialization exception
            assertThat(training.getTrainingType()).isNotNull();
            assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo(TrainingTypeName.CARDIO);

            assertThat(training.getTrainee()).isNotNull();
            assertThat(training.getTrainee().getUser()).isNotNull();
            assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("trainee1");
            assertThat(training.getTrainee().getUser().getFirstName()).isEqualTo("John");

            assertThat(training.getTrainer()).isNotNull();
            assertThat(training.getTrainer().getUser()).isNotNull();
            assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("trainer1");
            assertThat(training.getTrainer().getUser().getFirstName()).isEqualTo("Mike");
        }

        @Test
        @DisplayName("Should find training on exact fromDate")
        void shouldFindTrainingOnExactFromDate() {
            // Given
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    trainingDate, 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, trainingDate, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingDate()).isEqualTo(trainingDate);
        }

        @Test
        @DisplayName("Should find training on exact toDate")
        void shouldFindTrainingOnExactToDate() {
            // Given
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    trainingDate, 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, null, trainingDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingDate()).isEqualTo(trainingDate);
        }

        @Test
        @DisplayName("Should find training when fromDate equals toDate")
        void shouldFindTrainingWhenFromDateEqualsToDate() {
            // Given
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    trainingDate, 60);
            createTraining("Training 2", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 16), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, trainingDate, trainingDate);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingDate()).isEqualTo(trainingDate);
        }

        @Test
        @DisplayName("Should handle multiple trainings on same date")
        void shouldHandleMultipleTrainingsOnSameDate() {
            // Given
            LocalDate sameDate = LocalDate.of(2024, 6, 15);
            createTraining("Morning Training", trainee1, trainer1, cardioType,
                    sameDate, 60);
            createTraining("Evening Training", trainee1, trainer2, strengthType,
                    sameDate, 45);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", null, sameDate, sameDate);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Training::getTrainingName)
                    .containsExactlyInAnyOrder("Morning Training", "Evening Training");
        }
    }

    @Nested
    @DisplayName("JpaRepository Standard Methods Tests")
    class JpaRepositoryStandardMethodsTests {

        @Test
        @DisplayName("Should save and retrieve training")
        void shouldSaveAndRetrieveTraining() {
            // Given
            Training training = Training.builder()
                    .trainingName("New Training")
                    .trainee(trainee1)
                    .trainer(trainer1)
                    .trainingType(cardioType)
                    .trainingDate(LocalDate.of(2024, 6, 1))
                    .trainingDurationMinutes(60)
                    .build();

            // When
            Training savedTraining = trainingRepository.save(training);
            entityManager.flush();
            entityManager.clear();

            Optional<Training> retrieved = trainingRepository.findById(savedTraining.getId());

            // Then
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTrainingName()).isEqualTo("New Training");
        }

        @Test
        @DisplayName("Should find all trainings")
        void shouldFindAllTrainings() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findAll();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should delete training")
        void shouldDeleteTraining() {
            // Given
            Training training = createTraining("To Delete", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            Long trainingId = training.getId();

            // When
            trainingRepository.deleteById(trainingId);
            entityManager.flush();
            entityManager.clear();

            // Then
            Optional<Training> result = trainingRepository.findById(trainingId);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should check if training exists by id")
        void shouldCheckIfTrainingExistsById() {
            // Given
            Training training = createTraining("Existing Training", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();

            // When & Then
            assertThat(trainingRepository.existsById(training.getId())).isTrue();
            assertThat(trainingRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should count trainings")
        void shouldCountTrainings() {
            // Given
            createTraining("Training 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("Training 2", trainee2, trainer2, strengthType,
                    LocalDate.of(2024, 6, 15), 45);
            createTraining("Training 3", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 7, 1), 30);
            entityManager.flush();

            // When
            long count = trainingRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should update training")
        void shouldUpdateTraining() {
            // Given
            Training training = createTraining("Original Name", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            Training toUpdate = trainingRepository.findById(training.getId()).orElseThrow();
            toUpdate.setTrainingName("Updated Name");
            toUpdate.setTrainingDurationMinutes(90);
            trainingRepository.save(toUpdate);
            entityManager.flush();
            entityManager.clear();

            // Then
            Training updated = trainingRepository.findById(training.getId()).orElseThrow();
            assertThat(updated.getTrainingName()).isEqualTo("Updated Name");
            assertThat(updated.getTrainingDurationMinutes()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle training with minimum duration")
        void shouldHandleTrainingWithMinimumDuration() {
            // Given
            createTraining("Quick Training", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 1);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingDurationMinutes()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle training with long name")
        void shouldHandleTrainingWithLongName() {
            // Given
            String longName = "A".repeat(200);
            createTraining(longName, trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", null, null, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTrainingName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Should handle training on boundary dates")
        void shouldHandleTrainingOnBoundaryDates() {
            // Given
            LocalDate startOfYear = LocalDate.of(2024, 1, 1);
            LocalDate endOfYear = LocalDate.of(2024, 12, 31);

            createTraining("New Year Training", trainee1, trainer1, cardioType,
                    startOfYear, 60);
            createTraining("End Year Training", trainee1, trainer1, cardioType,
                    endOfYear, 60);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, null, startOfYear, endOfYear);

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle same trainee with multiple trainers")
        void shouldHandleSameTraineeWithMultipleTrainers() {
            // Given
            createTraining("With Trainer 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("With Trainer 2", trainee1, trainer2, strengthType,
                    LocalDate.of(2024, 6, 2), 45);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    "trainee1", null, null, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainer().getUser().getUsername())
                    .containsExactlyInAnyOrder("trainer1", "trainer2");
        }

        @Test
        @DisplayName("Should handle same trainer with multiple trainees")
        void shouldHandleSameTrainerWithMultipleTrainees() {
            // Given
            createTraining("With Trainee 1", trainee1, trainer1, cardioType,
                    LocalDate.of(2024, 6, 1), 60);
            createTraining("With Trainee 2", trainee2, trainer1, cardioType,
                    LocalDate.of(2024, 6, 2), 45);
            entityManager.flush();
            entityManager.clear();

            // When
            List<Training> result = trainingRepository.findTrainingsWithAllUsers(
                    null, "trainer1", null, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getTrainee().getUser().getUsername())
                    .containsExactlyInAnyOrder("trainee1", "trainee2");
        }
    }

    // Helper methods for creating test entities

    private TrainingType createTrainingType(TrainingTypeName name) {
        TrainingType trainingType = new TrainingType(null, name);
        return entityManager.persist(trainingType);
    }

    private User createUser(String username, String firstName, String lastName) {
        User user = User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password("password123")
                .isActive(true)
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

    private Training createTraining(String name, Trainee trainee, Trainer trainer,
                                    TrainingType trainingType, LocalDate date, int durationMinutes) {
        Training training = Training.builder()
                .trainingName(name)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .trainingDate(date)
                .trainingDurationMinutes(durationMinutes)
                .build();
        return entityManager.persist(training);
    }
}