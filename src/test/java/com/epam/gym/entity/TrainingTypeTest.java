package com.epam.gym.entity;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainingType Entity Tests")
class TrainingTypeTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final TrainingTypeName TYPE_NAME = TrainingTypeName.FITNESS;
    private static final TrainingTypeName TYPE_NAME_2 = TrainingTypeName.YOGA;

    @Test
    @DisplayName("Should create using no-args constructor")
    void shouldCreateUsingNoArgsConstructor() {
        TrainingType type = new TrainingType();

        assertNotNull(type);
        assertNull(type.getId());
        assertNull(type.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should create using all-args constructor")
    void shouldCreateUsingAllArgsConstructor() {
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        assertEquals(ID, type.getId());
        assertEquals(TYPE_NAME, type.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        assertEquals(type, type);
    }

    @Test
    @DisplayName("Should be equal with same id")
    void shouldBeEqualWithSameId() {
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID, TYPE_NAME_2);

        assertEquals(type1, type2);
    }

    @Test
    @DisplayName("Should not be equal with different id")
    void shouldNotBeEqualWithDifferentId() {
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID_2, TYPE_NAME);

        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        assertNotEquals(null, type);
    }

    @Test
    @DisplayName("Should return same hashCode for same class")
    void shouldReturnSameHashCode() {
        TrainingType type1 = new TrainingType(ID, TYPE_NAME);
        TrainingType type2 = new TrainingType(ID_2, TYPE_NAME_2);

        // hashCode() returns getClass().hashCode() - same for all instances
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        TrainingType type = new TrainingType(ID, TYPE_NAME);

        String result = type.toString();

        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        TrainingType type1 = new TrainingType(null, TYPE_NAME);
        TrainingType type2 = new TrainingType(null, TYPE_NAME);

        // null id means not equal
        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("Should accept all enum values")
    void shouldAcceptAllEnumValues() {
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            TrainingType type = new TrainingType(ID, typeName);
            assertEquals(typeName, type.getTrainingTypeName());
        }
    }
}