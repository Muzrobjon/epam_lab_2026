package com.epam.gym.repository;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TrainerRepository Tests")
class TrainerRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TrainerRepository trainerRepository;

    private User trainerUser1;
    private User trainerUser2;
    private User trainerUser3;
    private User traineeUser;
    private TrainingType specialization;
    private Trainer trainer1;
    private Trainer trainer2;
    private Trainer trainer3;
    private Trainee trainee;

    // Use unique identifiers to avoid conflicts with existing data
    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        // Generate unique suffix for this test run
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        // Get or create training type
        specialization = getOrCreateTrainingType(TrainingTypeName.FITNESS);

        // Create trainer users with unique usernames
        trainerUser1 = User.builder()
                .username("john.doe." + uniqueSuffix)
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .isActive(true)
                .build();

        trainerUser2 = User.builder()
                .username("jane.smith." + uniqueSuffix)
                .firstName("Jane")
                .lastName("Smith")
                .password("password123")
                .isActive(true)
                .build();

        trainerUser3 = User.builder()
                .username("inactive.trainer." + uniqueSuffix)
                .firstName("Inactive")
                .lastName("Trainer")
                .password("password123")
                .isActive(false)
                .build();

        entityManager.persist(trainerUser1);
        entityManager.persist(trainerUser2);
        entityManager.persist(trainerUser3);

        // Create trainers
        trainer1 = Trainer.builder()
                .user(trainerUser1)
                .specialization(specialization)
                .build();

        trainer2 = Trainer.builder()
                .user(trainerUser2)
                .specialization(specialization)
                .build();

        trainer3 = Trainer.builder()
                .user(trainerUser3)
                .specialization(specialization)
                .build();

        entityManager.persist(trainer1);
        entityManager.persist(trainer2);
        entityManager.persist(trainer3);

        // Create trainee user
        traineeUser = User.builder()
                .username("trainee.user." + uniqueSuffix)
                .firstName("Trainee")
                .lastName("User")
                .password("password123")
                .isActive(true)
                .build();
        entityManager.persist(traineeUser);

        // Create trainee
        trainee = Trainee.builder()
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Test Address")
                .build();
        entityManager.persist(trainee);

        entityManager.flush();
        entityManager.clear();
    }

    private TrainingType getOrCreateTrainingType(TrainingTypeName typeName) {
        List<TrainingType> types = entityManager
                .createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", typeName)
                .getResultList();

        if (!types.isEmpty()) {
            return types.get(0);
        }

        TrainingType type = new TrainingType(null, typeName);
        entityManager.persist(type);
        return type;
    }

    @Nested
    @DisplayName("findByUser_Username Tests")
    class FindByUserUsernameTests {

        @Test
        @DisplayName("Should find trainer by username when exists")
        void shouldFindTrainerByUsername_WhenExists() {
            Optional<Trainer> result = trainerRepository.findByUser_Username("john.doe." + uniqueSuffix);

            assertThat(result).isPresent();
            assertThat(result.get().getUser().getUsername()).isEqualTo("john.doe." + uniqueSuffix);
            assertThat(result.get().getUser().getFirstName()).isEqualTo("John");
            assertThat(result.get().getUser().getLastName()).isEqualTo("Doe");
            assertThat(result.get().getSpecialization()).isNotNull();
            assertThat(result.get().getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should return empty when trainer username does not exist")
        void shouldReturnEmpty_WhenUsernameDoesNotExist() {
            Optional<Trainer> result = trainerRepository.findByUser_Username("nonexistent.user." + uniqueSuffix);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find inactive trainer by username")
        void shouldFindInactiveTrainerByUsername() {
            Optional<Trainer> result = trainerRepository.findByUser_Username("inactive.trainer." + uniqueSuffix);

            assertThat(result).isPresent();
            assertThat(result.get().getUser().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should eagerly load user and specialization")
        void shouldEagerlyLoadUserAndSpecialization() {
            Optional<Trainer> result = trainerRepository.findByUser_Username("john.doe." + uniqueSuffix);

            assertThat(result).isPresent();
            Trainer trainer = result.get();

            assertThat(trainer.getUser()).isNotNull();
            assertThat(trainer.getUser().getUsername()).isEqualTo("john.doe." + uniqueSuffix);
            assertThat(trainer.getSpecialization()).isNotNull();
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should return empty for null username")
        void shouldReturnEmpty_ForNullUsername() {
            Optional<Trainer> result = trainerRepository.findByUser_Username(null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUser_UsernameIn Tests")
    class FindByUserUsernameInTests {

        @Test
        @DisplayName("Should find all trainers by usernames list")
        void shouldFindAllTrainersByUsernamesList() {
            List<String> usernames = List.of("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix);

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should return empty list when no usernames match")
        void shouldReturnEmptyList_WhenNoUsernamesMatch() {
            List<String> usernames = List.of("nonexistent1." + uniqueSuffix, "nonexistent2." + uniqueSuffix);

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find only matching trainers from mixed list")
        void shouldFindOnlyMatchingTrainers_FromMixedList() {
            List<String> usernames = List.of("john.doe." + uniqueSuffix, "nonexistent.user." + uniqueSuffix);

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getUsername()).isEqualTo("john.doe." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should return empty list for empty usernames list")
        void shouldReturnEmptyList_ForEmptyUsernamesList() {
            List<String> usernames = List.of();

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find single trainer for single username")
        void shouldFindSingleTrainer_ForSingleUsername() {
            List<String> usernames = List.of("jane.smith." + uniqueSuffix);

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getUsername()).isEqualTo("jane.smith." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should include inactive trainers in results")
        void shouldIncludeInactiveTrainersInResults() {
            List<String> usernames = List.of("john.doe." + uniqueSuffix, "inactive.trainer." + uniqueSuffix);

            List<Trainer> result = trainerRepository.findByUser_UsernameIn(usernames);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .containsExactlyInAnyOrder("john.doe." + uniqueSuffix, "inactive.trainer." + uniqueSuffix);
        }
    }

    @Nested
    @DisplayName("findAvailableTrainers Tests")
    class FindAvailableTrainersTests {

        @Test
        @DisplayName("Should return active trainers when trainee has no trainers - contains our test trainers")
        void shouldReturnActiveTrainers_WhenTraineeHasNoTrainers() {
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);

            // Check that our test trainers are in the results
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .contains("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix);

            // Verify inactive trainer is not included
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("inactive.trainer." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should exclude trainers already assigned to trainee")
        void shouldExcludeTrainersAlreadyAssignedToTrainee() {
            // Get initial count of available trainers
            List<Trainer> initialAvailable = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);
            int initialCount = initialAvailable.size();

            // Assign trainer1 to trainee
            Trainee managedTrainee = entityManager.find(Trainee.class, trainee.getId());
            Trainer managedTrainer1 = entityManager.find(Trainer.class, trainer1.getId());
            managedTrainee.getTrainers().add(managedTrainer1);
            entityManager.flush();
            entityManager.clear();

            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);

            // Should have one less trainer
            assertThat(result).hasSize(initialCount - 1);

            // Should not contain the assigned trainer
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("john.doe." + uniqueSuffix);

            // Should still contain the unassigned trainer
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .contains("jane.smith." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should exclude all assigned trainers")
        void shouldExcludeAllAssignedTrainers() {
            // Assign both active trainers to trainee
            Trainee managedTrainee = entityManager.find(Trainee.class, trainee.getId());
            Trainer managedTrainer1 = entityManager.find(Trainer.class, trainer1.getId());
            Trainer managedTrainer2 = entityManager.find(Trainer.class, trainer2.getId());
            managedTrainee.getTrainers().add(managedTrainer1);
            managedTrainee.getTrainers().add(managedTrainer2);
            entityManager.flush();
            entityManager.clear();

            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);

            // Our test trainers should not be in the results
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should not include inactive trainers")
        void shouldNotIncludeInactiveTrainers() {
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);

            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("inactive.trainer." + uniqueSuffix);

            // All returned trainers should be active
            assertThat(result).allMatch(t -> t.getUser().getIsActive());
        }

        @Test
        @DisplayName("Should return trainers for non-existent trainee - contains our test trainers")
        void shouldReturnTrainers_ForNonExistentTrainee() {
            List<Trainer> result = trainerRepository.findAvailableTrainers("nonexistent.trainee." + uniqueSuffix);

            // Should contain our active test trainers
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .contains("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should eagerly load user and specialization for available trainers")
        void shouldEagerlyLoadUserAndSpecialization() {
            List<Trainer> result = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);

            assertThat(result).isNotEmpty();
            result.forEach(trainer -> {
                assertThat(trainer.getUser()).isNotNull();
                assertThat(trainer.getUser().getUsername()).isNotNull();
                assertThat(trainer.getSpecialization()).isNotNull();
                assertThat(trainer.getSpecialization().getTrainingTypeName()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("JpaRepository Inherited Methods Tests")
    class JpaRepositoryMethodsTests {

        @Test
        @DisplayName("Should save and retrieve trainer")
        void shouldSaveAndRetrieveTrainer() {
            User newUser = User.builder()
                    .username("new.trainer." + uniqueSuffix)
                    .firstName("New")
                    .lastName("Trainer")
                    .password("password123")
                    .isActive(true)
                    .build();
            entityManager.persist(newUser);

            Trainer newTrainer = Trainer.builder()
                    .user(newUser)
                    .specialization(specialization)
                    .build();

            Trainer savedTrainer = trainerRepository.save(newTrainer);
            entityManager.flush();
            entityManager.clear();

            Optional<Trainer> retrieved = trainerRepository.findById(savedTrainer.getId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getUser().getUsername()).isEqualTo("new.trainer." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should find all trainers - contains our test trainers")
        void shouldFindAllTrainers() {
            List<Trainer> result = trainerRepository.findAll();

            // Should contain at least our 3 test trainers
            assertThat(result.size()).isGreaterThanOrEqualTo(3);
            assertThat(result)
                    .extracting(t -> t.getUser().getUsername())
                    .contains("john.doe." + uniqueSuffix, "jane.smith." + uniqueSuffix, "inactive.trainer." + uniqueSuffix);
        }

        @Test
        @DisplayName("Should delete trainer by id")
        void shouldDeleteTrainerById() {
            Long trainerId = trainer1.getId();

            trainerRepository.deleteById(trainerId);
            entityManager.flush();
            entityManager.clear();

            Optional<Trainer> deleted = trainerRepository.findById(trainerId);
            assertThat(deleted).isEmpty();
        }

        @Test
        @DisplayName("Should check if trainer exists by id")
        void shouldCheckIfTrainerExistsById() {
            assertThat(trainerRepository.existsById(trainer1.getId())).isTrue();
            assertThat(trainerRepository.existsById(999999L)).isFalse();
        }

        @Test
        @DisplayName("Should count trainers - at least our test trainers")
        void shouldCountTrainers() {
            long count = trainerRepository.count();

            // Should have at least our 3 test trainers
            assertThat(count).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should find trainer by id")
        void shouldFindTrainerById() {
            Optional<Trainer> result = trainerRepository.findById(trainer1.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(trainer1.getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent id")
        void shouldReturnEmpty_ForNonExistentId() {
            Optional<Trainer> result = trainerRepository.findById(999999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle trainer with different specialization")
        void shouldHandleTrainerWithDifferentSpecialization() {
            TrainingType yogaType = getOrCreateTrainingType(TrainingTypeName.YOGA);

            User yogaTrainerUser = User.builder()
                    .username("yoga.trainer." + uniqueSuffix)
                    .firstName("Yoga")
                    .lastName("Trainer")
                    .password("password123")
                    .isActive(true)
                    .build();
            entityManager.persist(yogaTrainerUser);

            Trainer yogaTrainer = Trainer.builder()
                    .user(yogaTrainerUser)
                    .specialization(yogaType)
                    .build();
            entityManager.persist(yogaTrainer);
            entityManager.flush();
            entityManager.clear();

            Optional<Trainer> result = trainerRepository.findByUser_Username("yoga.trainer." + uniqueSuffix);

            assertThat(result).isPresent();
            assertThat(result.get().getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should handle multiple trainees with different assigned trainers")
        void shouldHandleMultipleTraineesWithDifferentAssignedTrainers() {
            // Create second trainee
            User traineeUser2 = User.builder()
                    .username("trainee.user2." + uniqueSuffix)
                    .firstName("Trainee2")
                    .lastName("User2")
                    .password("password123")
                    .isActive(true)
                    .build();
            entityManager.persist(traineeUser2);

            Trainee trainee2 = Trainee.builder()
                    .user(traineeUser2)
                    .dateOfBirth(LocalDate.of(1995, 5, 5))
                    .address("Test Address 2")
                    .build();
            entityManager.persist(trainee2);

            // Assign trainer1 to trainee1
            Trainee managedTrainee1 = entityManager.find(Trainee.class, trainee.getId());
            Trainer managedTrainer1 = entityManager.find(Trainer.class, trainer1.getId());
            managedTrainee1.getTrainers().add(managedTrainer1);

            // Assign trainer2 to trainee2
            Trainee managedTrainee2 = entityManager.find(Trainee.class, trainee2.getId());
            Trainer managedTrainer2 = entityManager.find(Trainer.class, trainer2.getId());
            managedTrainee2.getTrainers().add(managedTrainer2);

            entityManager.flush();
            entityManager.clear();

            // Check available trainers for trainee1 - should not contain trainer1
            List<Trainer> availableForTrainee1 = trainerRepository.findAvailableTrainers("trainee.user." + uniqueSuffix);
            assertThat(availableForTrainee1)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("john.doe." + uniqueSuffix)
                    .contains("jane.smith." + uniqueSuffix);

            // Check available trainers for trainee2 - should not contain trainer2
            List<Trainer> availableForTrainee2 = trainerRepository.findAvailableTrainers("trainee.user2." + uniqueSuffix);
            assertThat(availableForTrainee2)
                    .extracting(t -> t.getUser().getUsername())
                    .doesNotContain("jane.smith." + uniqueSuffix)
                    .contains("john.doe." + uniqueSuffix);
        }
    }
}