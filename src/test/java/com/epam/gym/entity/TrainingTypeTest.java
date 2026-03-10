package com.epam.gym.entity;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrainingType} entity.
 * Pure JUnit 5 - no external dependencies.
 */
@DisplayName("TrainingType Entity Tests")
class TrainingTypeTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final TrainingTypeName TYPE_NAME = TrainingTypeName.FITNESS;
    private static final TrainingTypeName TYPE_NAME_2 = TrainingTypeName.YOGA;

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create training type using no-args constructor")
    void shouldCreateTrainingTypeUsingNoArgsConstructor() {
        // When
        TrainingType trainingType = new TrainingType();

        // Then
        assertNotNull(trainingType);
        assertNull(trainingType.getId());
        assertNull(trainingType.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should create training type using all-args constructor")
    void shouldCreateTrainingTypeUsingAllArgsConstructor() {
        // When
        TrainingType trainingType = new TrainingType(ID, TYPE_NAME);

        // Then
        assertEquals(ID, trainingType.getId());
        assertEquals(TYPE_NAME, trainingType.getTrainingTypeName());
    }

    // ==================== Getter Tests ====================

    @Test
    @DisplayName("Should get id")
    void shouldGetId() {
        // Given
        TrainingType trainingType = new TrainingType(ID, TYPE_NAME);

        // Then
        assertEquals(ID, trainingType.getId());
    }

    @Test
    @DisplayName("Should get training type name")
    void shouldGetTrainingTypeName() {
        // Given
        TrainingType trainingType = new TrainingType(ID, TYPE_NAME);

        // Then
        assertEquals(TYPE_NAME, trainingType.getTrainingTypeName());
    }

    // ==================== Immutability Tests ====================

    @Test
    @DisplayName("Should not have setter for id")
    void shouldNotHaveSetterForId() {
        // Given
        new TrainingType();

        // Then - verify no setter method exists
        assertThrows(NoSuchMethodException.class, () -> TrainingType.class.getMethod("setId", Long.class));
    }

    @Test
    @DisplayName("Should not have setter for training type name")
    void shouldNotHaveSetterForTrainingTypeName() {
        // Then - verify no setter method exists
        assertThrows(NoSuchMethodException.class, () -> TrainingType.class.getMethod("setTrainingTypeName", TrainingTypeName.class));
    }

    @Test
    @DisplayName("Should not have builder")
    void shouldNotHaveBuilder() {
        // Then - verify no builder method exists
        assertThrows(NoSuchMethodException.class, () -> TrainingType.class.getMethod("builder"));
    }

    // ==================== Equals & HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // Then
        assertEquals(type, type);
    }

    @Test
    @DisplayName("Should be equal to another type with same id")
    void shouldBeEqualToAnotherTypeWithSameId() {
        // Given
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID, TYPE_NAME_2);

        // Then - equals only checks id
        assertEquals(type1, type2);
    }

    @Test
    @DisplayName("Should not be equal to type with different id")
    void shouldNotBeEqualToTypeWithDifferentId() {
        // Given
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID_2, TYPE_NAME);

        // Then
        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // Then
        assertNotEquals(null, type);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // Then
        assertNotEquals("not a training type", type);
    }

    @Test
    @DisplayName("Should return same hash code for equal objects")
    void shouldReturnSameHashCodeForEqualObjects() {
        // Given
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID, TYPE_NAME_2);

        // Then
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);
        int hashCode1 = type.hashCode();
        int hashCode2 = type.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // When
        String result = type.toString();

        // Then
        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should include training type name in toString")
    void shouldIncludeTrainingTypeNameInToString() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // When
        String result = type.toString();

        // Then
        assertTrue(result.contains("trainingTypeName=" + TYPE_NAME));
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        TrainingType type1 = new TrainingType(null, TYPE_NAME);
        TrainingType type2 = new TrainingType(null, TYPE_NAME);

        // Then
        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("Should handle null training type name")
    void shouldHandleNullTrainingTypeName() {
        // Given
        TrainingType type = new TrainingType(ID, null);

        // Then
        assertNull(type.getTrainingTypeName());
    }

    // ==================== All Enum Values Tests ====================

    @Test
    @DisplayName("Should accept all training type enum values")
    void shouldAcceptAllTrainingTypeEnumValues() {
        // When & Then
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            TrainingType type = new TrainingType(ID, typeName);
            assertEquals(typeName, type.getTrainingTypeName());
        }
    }

    // ==================== Database Constraint Simulation Tests ====================

    @Test
    @DisplayName("Should demonstrate unique constraint concept")
    void shouldDemonstrateUniqueConstraintConcept() {
        // Given - two types with same name but different ids (would violate DB constraint)
        TrainingType type1 = new TrainingType(1L, TYPE_NAME);
        TrainingType type2 = new TrainingType(2L, TYPE_NAME);

        // Then - they are not equal (different ids), but same name
        assertNotEquals(type1, type2);
        assertEquals(type1.getTrainingTypeName(), type2.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should demonstrate immutable concept - fields cannot change")
    void shouldDemonstrateImmutableConcept() {
        // Given
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        // Then - fields are set at construction and cannot be modified
        // No setters available, only getters
        assertEquals(ID, type.getId());
        assertEquals(TYPE_NAME, type.getTrainingTypeName());
    }
}