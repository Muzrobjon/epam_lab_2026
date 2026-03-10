package com.epam.gym.entity;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Training} entity.
 * Pure JUnit 5 - no external dependencies.
 */
@DisplayName("Training Entity Tests")
class TrainingTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final String TRAINING_NAME = "Morning Yoga";
    private static final String TRAINING_NAME_2 = "Evening Cardio";
    private static final TrainingTypeName TRAINING_TYPE = TrainingTypeName.YOGA;
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 3, 15);
    private static final Integer DURATION_MINUTES = 60;

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create training using no-args constructor")
    void shouldCreateTrainingUsingNoArgsConstructor() {
        // When
        Training training = new Training();

        // Then
        assertNotNull(training);
        assertNull(training.getId());
        assertNull(training.getTrainee());
        assertNull(training.getTrainer());
        assertNull(training.getTrainingName());
        assertNull(training.getTrainingType());
        assertNull(training.getTrainingDate());
        assertNull(training.getTrainingDurationMinutes());
    }

    @Test
    @DisplayName("Should create training using all-args constructor")
    void shouldCreateTrainingUsingAllArgsConstructor() {
        // Given
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        // When
        Training training = new Training(
                ID, trainee, trainer, TRAINING_NAME,
                TRAINING_TYPE, TRAINING_DATE, DURATION_MINUTES
        );

        // Then
        assertEquals(ID, training.getId());
        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals(TRAINING_NAME, training.getTrainingName());
        assertEquals(TRAINING_TYPE, training.getTrainingType());
        assertEquals(TRAINING_DATE, training.getTrainingDate());
        assertEquals(DURATION_MINUTES, training.getTrainingDurationMinutes());
    }

    @Test
    @DisplayName("Should create training using builder")
    void shouldCreateTrainingUsingBuilder() {
        // Given
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        // When
        Training training = Training.builder()
                .id(ID)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(TRAINING_NAME)
                .trainingType(TRAINING_TYPE)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(DURATION_MINUTES)
                .build();

        // Then
        assertEquals(ID, training.getId());
        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals(TRAINING_NAME, training.getTrainingName());
        assertEquals(TRAINING_TYPE, training.getTrainingType());
        assertEquals(TRAINING_DATE, training.getTrainingDate());
        assertEquals(DURATION_MINUTES, training.getTrainingDurationMinutes());
    }

    // ==================== Getter & Setter Tests ====================

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        Training training = new Training();

        // When
        training.setId(ID);

        // Then
        assertEquals(ID, training.getId());
    }

    @Test
    @DisplayName("Should set and get trainee")
    void shouldSetAndGetTrainee() {
        // Given
        Training training = new Training();
        Trainee trainee = new Trainee();

        // When
        training.setTrainee(trainee);

        // Then
        assertEquals(trainee, training.getTrainee());
    }

    @Test
    @DisplayName("Should set and get trainer")
    void shouldSetAndGetTrainer() {
        // Given
        Training training = new Training();
        Trainer trainer = new Trainer();

        // When
        training.setTrainer(trainer);

        // Then
        assertEquals(trainer, training.getTrainer());
    }

    @Test
    @DisplayName("Should set and get training name")
    void shouldSetAndGetTrainingName() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingName(TRAINING_NAME);

        // Then
        assertEquals(TRAINING_NAME, training.getTrainingName());
    }

    @Test
    @DisplayName("Should set and get training type")
    void shouldSetAndGetTrainingType() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingType(TRAINING_TYPE);

        // Then
        assertEquals(TRAINING_TYPE, training.getTrainingType());
    }

    @Test
    @DisplayName("Should set and get training date")
    void shouldSetAndGetTrainingDate() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingDate(TRAINING_DATE);

        // Then
        assertEquals(TRAINING_DATE, training.getTrainingDate());
    }

    @Test
    @DisplayName("Should set and get training duration minutes")
    void shouldSetAndGetTrainingDurationMinutes() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingDurationMinutes(DURATION_MINUTES);

        // Then
        assertEquals(DURATION_MINUTES, training.getTrainingDurationMinutes());
    }

    // ==================== Equals & HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // Then
        assertEquals(training, training);
    }

    @Test
    @DisplayName("Should be equal to another training with same id")
    void shouldBeEqualToAnotherTrainingWithSameId() {
        // Given
        Training training1 = createTestTraining(ID, TRAINING_NAME);
        Training training2 = createTestTraining(ID, TRAINING_NAME_2);

        // Then - equals only checks id, not other fields
        assertEquals(training1, training2);
    }

    @Test
    @DisplayName("Should not be equal to training with different id")
    void shouldNotBeEqualToTrainingWithDifferentId() {
        // Given
        Training training1 = createTestTraining(ID, TRAINING_NAME);
        Training training2 = createTestTraining(ID_2, TRAINING_NAME);

        // Then
        assertNotEquals(training1, training2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // Then
        assertNotEquals(null, training);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // Then
        assertNotEquals("not a training", training);
    }

    @Test
    @DisplayName("Should return same hash code for equal objects")
    void shouldReturnSameHashCodeForEqualObjects() {
        // Given
        Training training1 = createTestTraining(ID, TRAINING_NAME);
        Training training2 = createTestTraining(ID, TRAINING_NAME_2);

        // Then
        assertEquals(training1.hashCode(), training2.hashCode());
    }

    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);
        int hashCode1 = training.hashCode();
        int hashCode2 = training.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // When
        String result = training.toString();

        // Then
        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should include training name in toString")
    void shouldIncludeTrainingNameInToString() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // When
        String result = training.toString();

        // Then
        assertTrue(result.contains("trainingName='" + TRAINING_NAME + "'"));
    }

    @Test
    @DisplayName("Should include training type in toString")
    void shouldIncludeTrainingTypeInToString() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // When
        String result = training.toString();

        // Then
        assertTrue(result.contains("trainingType=" + TRAINING_TYPE));
    }

    @Test
    @DisplayName("Should include training date in toString")
    void shouldIncludeTrainingDateInToString() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // When
        String result = training.toString();

        // Then
        assertTrue(result.contains("trainingDate=" + TRAINING_DATE));
    }

    @Test
    @DisplayName("Should include training duration in toString")
    void shouldIncludeTrainingDurationInToString() {
        // Given
        Training training = createTestTraining(ID, TRAINING_NAME);

        // When
        String result = training.toString();

        // Then
        assertTrue(result.contains("trainingDurationMinutes=" + DURATION_MINUTES));
    }

    @Test
    @DisplayName("Should not include trainee in toString")
    void shouldNotIncludeTraineeInToString() {
        // Given
        Trainee trainee = new Trainee();
        Training training = Training.builder()
                .id(ID)
                .trainee(trainee)
                .trainingName(TRAINING_NAME)
                .trainingType(TRAINING_TYPE)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(DURATION_MINUTES)
                .build();

        // When
        String result = training.toString();

        // Then
        assertFalse(result.contains("trainee="));
    }

    @Test
    @DisplayName("Should not include trainer in toString")
    void shouldNotIncludeTrainerInToString() {
        // Given
        Trainer trainer = new Trainer();
        Training training = Training.builder()
                .id(ID)
                .trainer(trainer)
                .trainingName(TRAINING_NAME)
                .trainingType(TRAINING_TYPE)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(DURATION_MINUTES)
                .build();

        // When
        String result = training.toString();

        // Then
        assertFalse(result.contains("trainer="));
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        Training training1 = createTestTraining(null, TRAINING_NAME);
        Training training2 = createTestTraining(null, TRAINING_NAME);

        // Then
        assertNotEquals(training1, training2);
    }

    @Test
    @DisplayName("Should handle null training name")
    void shouldHandleNullTrainingName() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingName(null);

        // Then
        assertNull(training.getTrainingName());
    }

    @Test
    @DisplayName("Should handle null training type")
    void shouldHandleNullTrainingType() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingType(null);

        // Then
        assertNull(training.getTrainingType());
    }

    @Test
    @DisplayName("Should handle null training date")
    void shouldHandleNullTrainingDate() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingDate(null);

        // Then
        assertNull(training.getTrainingDate());
    }

    @Test
    @DisplayName("Should handle null duration")
    void shouldHandleNullDuration() {
        // Given
        Training training = new Training();

        // When
        training.setTrainingDurationMinutes(null);

        // Then
        assertNull(training.getTrainingDurationMinutes());
    }

    @Test
    @DisplayName("Should handle null trainee")
    void shouldHandleNullTrainee() {
        // Given
        Training training = new Training();

        // When
        training.setTrainee(null);

        // Then
        assertNull(training.getTrainee());
    }

    @Test
    @DisplayName("Should handle null trainer")
    void shouldHandleNullTrainer() {
        // Given
        Training training = new Training();

        // When
        training.setTrainer(null);

        // Then
        assertNull(training.getTrainer());
    }

    // ==================== TrainingTypeName Enum Tests ====================

    @Test
    @DisplayName("Should accept all training types")
    void shouldAcceptAllTrainingTypes() {
        // Given
        Training training = new Training();

        // When & Then
        for (TrainingTypeName type : TrainingTypeName.values()) {
            training.setTrainingType(type);
            assertEquals(type, training.getTrainingType());
        }
    }

    // ==================== Helper Methods ====================

    private Training createTestTraining(Long id, String trainingName) {
        return Training.builder()
                .id(id)
                .trainingName(trainingName)
                .trainingType(TRAINING_TYPE)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(DURATION_MINUTES)
                .build();
    }
}