package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeTest {

    @Test
    void shouldCreateTrainingTypeUsingBuilder() {
        TrainingType type = TrainingType.builder()
                .trainingTypeName("Strength")
                .build();

        assertNotNull(type);
        assertEquals("Strength", type.getTrainingTypeName());
    }

    @Test
    void shouldSetAndGetProperties() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Cardio");
        type.setTrainingTypeId(1L);

        assertEquals("Cardio", type.getTrainingTypeName());
        assertEquals(1L, type.getTrainingTypeId());
    }
}