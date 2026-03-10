package com.epam.gym.entity;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Trainer} entity.
 * Pure JUnit 5 - no external dependencies.
 */
@DisplayName("Trainer Entity Tests")
class TrainerTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final TrainingTypeName SPECIALIZATION = TrainingTypeName.FITNESS;

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create trainer using no-args constructor")
    void shouldCreateTrainerUsingNoArgsConstructor() {
        // When
        Trainer trainer = new Trainer();

        // Then
        assertNotNull(trainer);
        assertNull(trainer.getId());
        assertNull(trainer.getUser());
        assertNull(trainer.getSpecialization());
        assertNotNull(trainer.getTrainings());
        assertTrue(trainer.getTrainings().isEmpty());
        assertNotNull(trainer.getTrainees());
        assertTrue(trainer.getTrainees().isEmpty());
    }

    @Test
    @DisplayName("Should create trainer using all-args constructor")
    void shouldCreateTrainerUsingAllArgsConstructor() {
        // Given
        User user = new User();
        List<Training> trainings = new ArrayList<>();
        List<Trainee> trainees = new ArrayList<>();

        // When
        Trainer trainer = new Trainer(ID, user, SPECIALIZATION, trainings, trainees);

        // Then
        assertEquals(ID, trainer.getId());
        assertEquals(user, trainer.getUser());
        assertEquals(SPECIALIZATION, trainer.getSpecialization());
        assertEquals(trainings, trainer.getTrainings());
        assertEquals(trainees, trainer.getTrainees());
    }

    @Test
    @DisplayName("Should create trainer using builder")
    void shouldCreateTrainerUsingBuilder() {
        // Given
        User user = new User();

        // When
        Trainer trainer = Trainer.builder()
                .id(ID)
                .user(user)
                .specialization(SPECIALIZATION)
                .build();

        // Then
        assertEquals(ID, trainer.getId());
        assertEquals(user, trainer.getUser());
        assertEquals(SPECIALIZATION, trainer.getSpecialization());
        assertNotNull(trainer.getTrainings());
        assertNotNull(trainer.getTrainees());
    }

    @Test
    @DisplayName("Should create trainer with builder default collections")
    void shouldCreateTrainerWithBuilderDefaultCollections() {
        // When
        Trainer trainer = Trainer.builder()
                .id(ID)
                .specialization(SPECIALIZATION)
                .build();

        // Then
        assertNotNull(trainer.getTrainings());
        assertNotNull(trainer.getTrainees());
        assertTrue(trainer.getTrainings().isEmpty());
        assertTrue(trainer.getTrainees().isEmpty());
    }

    // ==================== Getter & Setter Tests ====================

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        Trainer trainer = new Trainer();

        // When
        trainer.setId(ID);

        // Then
        assertEquals(ID, trainer.getId());
    }

    @Test
    @DisplayName("Should set and get user")
    void shouldSetAndGetUser() {
        // Given
        Trainer trainer = new Trainer();
        User user = new User();

        // When
        trainer.setUser(user);

        // Then
        assertEquals(user, trainer.getUser());
    }

    @Test
    @DisplayName("Should set and get specialization")
    void shouldSetAndGetSpecialization() {
        // Given
        Trainer trainer = new Trainer();

        // When
        trainer.setSpecialization(SPECIALIZATION);

        // Then
        assertEquals(SPECIALIZATION, trainer.getSpecialization());
    }

    @Test
    @DisplayName("Should set and get trainings list")
    void shouldSetAndGetTrainingsList() {
        // Given
        Trainer trainer = new Trainer();
        List<Training> trainings = new ArrayList<>();
        trainings.add(new Training());

        // When
        trainer.setTrainings(trainings);

        // Then
        assertEquals(trainings, trainer.getTrainings());
        assertEquals(1, trainer.getTrainings().size());
    }

    @Test
    @DisplayName("Should set and get trainees list")
    void shouldSetAndGetTraineesList() {
        // Given
        Trainer trainer = new Trainer();
        List<Trainee> trainees = new ArrayList<>();
        trainees.add(new Trainee());

        // When
        trainer.setTrainees(trainees);

        // Then
        assertEquals(trainees, trainer.getTrainees());
        assertEquals(1, trainer.getTrainees().size());
    }

    // ==================== Collection Manipulation Tests ====================

    @Test
    @DisplayName("Should add training to trainings list")
    void shouldAddTrainingToTrainingsList() {
        // Given
        Trainer trainer = new Trainer();
        Training training = new Training();

        // When
        trainer.getTrainings().add(training);

        // Then
        assertEquals(1, trainer.getTrainings().size());
        assertTrue(trainer.getTrainings().contains(training));
    }

    @Test
    @DisplayName("Should add trainee to trainees list")
    void shouldAddTraineeToTraineesList() {
        // Given
        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();

        // When
        trainer.getTrainees().add(trainee);

        // Then
        assertEquals(1, trainer.getTrainees().size());
        assertTrue(trainer.getTrainees().contains(trainee));
    }

    @Test
    @DisplayName("Should remove training from trainings list")
    void shouldRemoveTrainingFromTrainingsList() {
        // Given
        Trainer trainer = new Trainer();
        Training training = new Training();
        trainer.getTrainings().add(training);

        // When
        trainer.getTrainings().remove(training);

        // Then
        assertTrue(trainer.getTrainings().isEmpty());
    }

    @Test
    @DisplayName("Should remove trainee from trainees list")
    void shouldRemoveTraineeFromTraineesList() {
        // Given
        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();
        trainer.getTrainees().add(trainee);

        // When
        trainer.getTrainees().remove(trainee);

        // Then
        assertTrue(trainer.getTrainees().isEmpty());
    }

    // ==================== Equals & HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);

        // Then
        assertEquals(trainer, trainer);
    }

    @Test
    @DisplayName("Should be equal to another trainer with same id")
    void shouldBeEqualToAnotherTrainerWithSameId() {
        // Given
        Trainer trainer1 = createTestTrainer(ID, SPECIALIZATION);
        Trainer trainer2 = createTestTrainer(ID, TrainingTypeName.YOGA);

        // Then - equals only checks id, not specialization
        assertEquals(trainer1, trainer2);
    }

    @Test
    @DisplayName("Should not be equal to trainer with different id")
    void shouldNotBeEqualToTrainerWithDifferentId() {
        // Given
        Trainer trainer1 = createTestTrainer(ID, SPECIALIZATION);
        Trainer trainer2 = createTestTrainer(ID_2, SPECIALIZATION);

        // Then
        assertNotEquals(trainer1, trainer2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);

        // Then
        assertNotEquals(null, trainer);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);

        // Then
        assertNotEquals("not a trainer", trainer);
    }

    @Test
    @DisplayName("Should return same hash code for equal objects")
    void shouldReturnSameHashCodeForEqualObjects() {
        // Given
        Trainer trainer1 = createTestTrainer(ID, SPECIALIZATION);
        Trainer trainer2 = createTestTrainer(ID, TrainingTypeName.YOGA);

        // Then
        assertEquals(trainer1.hashCode(), trainer2.hashCode());
    }

    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);
        int hashCode1 = trainer.hashCode();
        int hashCode2 = trainer.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);

        // When
        String result = trainer.toString();

        // Then
        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should include specialization in toString")
    void shouldIncludeSpecializationInToString() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);

        // When
        String result = trainer.toString();

        // Then
        assertTrue(result.contains("specialization=" + SPECIALIZATION));
    }

    @Test
    @DisplayName("Should not include user in toString")
    void shouldNotIncludeUserInToString() {
        // Given
        User user = new User();
        Trainer trainer = Trainer.builder()
                .id(ID)
                .user(user)
                .specialization(SPECIALIZATION)
                .build();

        // When
        String result = trainer.toString();

        // Then
        assertFalse(result.contains("user="));
    }

    @Test
    @DisplayName("Should not include collections in toString")
    void shouldNotIncludeCollectionsInToString() {
        // Given
        Trainer trainer = createTestTrainer(ID, SPECIALIZATION);
        trainer.getTrainings().add(new Training());
        trainer.getTrainees().add(new Trainee());

        // When
        String result = trainer.toString();

        // Then
        assertFalse(result.contains("trainings="));
        assertFalse(result.contains("trainees="));
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        Trainer trainer1 = createTestTrainer(null, SPECIALIZATION);
        Trainer trainer2 = createTestTrainer(null, SPECIALIZATION);

        // Then - both have null id
        assertNotEquals(trainer1, trainer2);
    }

    @Test
    @DisplayName("Should handle null specialization")
    void shouldHandleNullSpecialization() {
        // Given
        Trainer trainer = new Trainer();

        // When
        trainer.setSpecialization(null);

        // Then
        assertNull(trainer.getSpecialization());
    }

    @Test
    @DisplayName("Should handle null user")
    void shouldHandleNullUser() {
        // Given
        Trainer trainer = new Trainer();

        // When
        trainer.setUser(null);

        // Then
        assertNull(trainer.getUser());
    }

    // ==================== TrainingTypeName Enum Tests ====================

    @Test
    @DisplayName("Should accept all training type specializations")
    void shouldAcceptAllTrainingTypeSpecializations() {
        // Given
        Trainer trainer = new Trainer();

        // When & Then
        for (TrainingTypeName type : TrainingTypeName.values()) {
            trainer.setSpecialization(type);
            assertEquals(type, trainer.getSpecialization());
        }
    }

    // ==================== Helper Methods ====================

    private Trainer createTestTrainer(Long id, TrainingTypeName specialization) {
        return Trainer.builder()
                .id(id)
                .specialization(specialization)
                .build();
    }
}