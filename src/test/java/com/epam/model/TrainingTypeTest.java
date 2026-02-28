package com.epam.model;

import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeTest {

    @Test
    void trainingTypeBuilder_ShouldCreateTrainingType() {
        TrainingType type = TrainingType.builder()
                .id(1L)
                .trainingTypeName("Fitness")
                .build();

        assertEquals(1L, type.getId());
        assertEquals("Fitness", type.getTrainingTypeName());
    }

    @Test
    void trainingTypeNoArgsConstructor_ShouldCreateEmptyType() {
        TrainingType type = new TrainingType();

        assertNull(type.getId());
        assertNull(type.getTrainingTypeName());
    }

    @Test
    void trainingTypeAllArgsConstructor_ShouldCreateType() {
        TrainingType type = new TrainingType(1L, "Yoga");

        assertEquals(1L, type.getId());
        assertEquals("Yoga", type.getTrainingTypeName());
    }

    @Test
    void trainingTypeSettersAndGetters_ShouldWork() {
        TrainingType type = new TrainingType();
        type.setId(2L);
        type.setTrainingTypeName("Cardio");

        assertEquals(2L, type.getId());
        assertEquals("Cardio", type.getTrainingTypeName());
    }
}
