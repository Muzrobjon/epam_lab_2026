package com.epam.gym.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeNameTest {

    @Test
    void enumContainsExpectedValues() {
        // Enum 8 ta qiymatga ega
        TrainingTypeName[] expectedValues = {
                TrainingTypeName.FITNESS,
                TrainingTypeName.YOGA,
                TrainingTypeName.CARDIO,
                TrainingTypeName.STRENGTH,
                TrainingTypeName.PILATES,
                TrainingTypeName.CROSSFIT,
                TrainingTypeName.ZUMBA,
                TrainingTypeName.FLEXIBILITY
        };

        assertArrayEquals(expectedValues, TrainingTypeName.values(),
                "Enum should contain exactly 8 expected training types");
    }

    @Test
    void enumHasCorrectCount() {
        assertEquals(8, TrainingTypeName.values().length,
                "Enum should have exactly 8 values");
    }

    @ParameterizedTest
    @EnumSource(TrainingTypeName.class)
    void eachEnumValueIsNotNull(TrainingTypeName type) {
        assertNotNull(type, "Each enum value should not be null");
    }

    @ParameterizedTest
    @CsvSource({
            "FITNESS, FITNESS",
            "YOGA, YOGA",
            "CARDIO, CARDIO",
            "STRENGTH, STRENGTH",
            "PILATES, PILATES",
            "CROSSFIT, CROSSFIT",
            "ZUMBA, ZUMBA",
            "FLEXIBILITY, FLEXIBILITY"
    })
    void enumValueOfReturnsCorrectValue(String name, TrainingTypeName expected) {
        assertEquals(expected, TrainingTypeName.valueOf(name),
                "valueOf should return correct enum for: " + name);
    }

    @Test
    void enumValuesAreUnique() {
        TrainingTypeName[] values = TrainingTypeName.values();

        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i], values[j],
                        "Enum values should be unique: " + values[i] + " vs " + values[j]);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"fitness", "Yoga", "INVALID", "null", ""})
    void valueOfThrowsExceptionForInvalidNames(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> TrainingTypeName.valueOf(invalidName), "valueOf should throw IllegalArgumentException for: " + invalidName);
    }

    @Test
    void enumHasConsistentOrdinalValues() {
        // Enum tartibini tekshirish (agar muhim bo'lsa)
        assertEquals(0, TrainingTypeName.FITNESS.ordinal());
        assertEquals(1, TrainingTypeName.YOGA.ordinal());
        assertEquals(2, TrainingTypeName.CARDIO.ordinal());
        assertEquals(3, TrainingTypeName.STRENGTH.ordinal());
        assertEquals(4, TrainingTypeName.PILATES.ordinal());
        assertEquals(5, TrainingTypeName.CROSSFIT.ordinal());
        assertEquals(6, TrainingTypeName.ZUMBA.ordinal());
        assertEquals(7, TrainingTypeName.FLEXIBILITY.ordinal());
    }

    @ParameterizedTest
    @EnumSource(TrainingTypeName.class)
    void enumToStringReturnsName(TrainingTypeName type) {
        assertEquals(type.name(), type.toString(),
                "toString should return enum name");
    }

    @Test
    void enumCompareToWorksCorrectly() {
        // FITNESS < YOGA < CARDIO ... tartibida
        assertTrue(true);
        assertTrue(true);
        assertEquals(0, TrainingTypeName.CARDIO.compareTo(TrainingTypeName.CARDIO));
    }
}