package com.epam.gym.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// TODO:
//  The tests are correct and well structured. However, most of them verify the built-in behavior of Java enums
//  rather than application logic. For a simple enum without additional fields or methods, a large number of tests
//  usually brings limited value. In such cases one small test verifying expected values is typically enough:
//  shouldContainAllExpectedTrainingTypes - that's it.
class TrainingTypeNameTest {

    @Test
    void enum_ShouldContainExactlyEightValues() {
        TrainingTypeName[] values = TrainingTypeName.values();
        assertEquals(8, values.length);
    }

    @Test
    void enum_ShouldContainAllExpectedTrainingTypes() {
        assertAll(
                () -> assertNotNull(TrainingTypeName.valueOf("FITNESS")),
                () -> assertNotNull(TrainingTypeName.valueOf("YOGA")),
                () -> assertNotNull(TrainingTypeName.valueOf("CARDIO")),
                () -> assertNotNull(TrainingTypeName.valueOf("STRENGTH")),
                () -> assertNotNull(TrainingTypeName.valueOf("PILATES")),
                () -> assertNotNull(TrainingTypeName.valueOf("CROSSFIT")),
                () -> assertNotNull(TrainingTypeName.valueOf("ZUMBA")),
                () -> assertNotNull(TrainingTypeName.valueOf("FLEXIBILITY"))
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnumForValidName() {
        assertEquals(TrainingTypeName.FITNESS, TrainingTypeName.valueOf("FITNESS"));
        assertEquals(TrainingTypeName.YOGA, TrainingTypeName.valueOf("YOGA"));
        assertEquals(TrainingTypeName.CARDIO, TrainingTypeName.valueOf("CARDIO"));
        assertEquals(TrainingTypeName.FLEXIBILITY, TrainingTypeName.valueOf("FLEXIBILITY"));
    }

    @Test
    void valueOf_ShouldThrowIllegalArgumentExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> TrainingTypeName.valueOf("INVALID"));
    }

    @Test
    void valueOf_ShouldBeCaseSensitive() {
        assertThrows(IllegalArgumentException.class, () -> TrainingTypeName.valueOf("fitness"));
    }

    @Test
    void enum_ShouldNotAllowNullName() {
        assertThrows(NullPointerException.class, () -> TrainingTypeName.valueOf(null));
    }
}