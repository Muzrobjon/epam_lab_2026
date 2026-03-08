package com.epam.gym.model;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTest {

    @Test
    void testEqualsAndHashCode() {
        Training t1 = Training.builder()
                .id(1L)
                .trainingName("Morning Cardio")
                .trainingType(TrainingTypeName.CARDIO)
                .trainingDate(LocalDate.of(2024, 1, 1))
                .trainingDurationMinutes(60)
                .build();

        Training t2 = Training.builder()
                .id(1L)
                .trainingName("Evening Strength")
                .trainingType(TrainingTypeName.STRENGTH)
                .trainingDate(LocalDate.of(2024, 2, 2))
                .trainingDurationMinutes(45)
                .build();

        Training t3 = Training.builder()
                .id(2L)
                .trainingName("Morning Cardio")
                .trainingType(TrainingTypeName.CARDIO)
                .trainingDate(LocalDate.of(2024, 1, 1))
                .trainingDurationMinutes(60)
                .build();

        assertEquals(t1, t2, "Trainings with same id should be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Hash codes should match for same id");
        assertNotEquals(t1, t3, "Trainings with different ids should not be equal");
    }

    @Test
    void testToString() {
        Training t = Training.builder()
                .id(1L)
                .trainingName("Morning Cardio")
                .trainingType(TrainingTypeName.CARDIO)
                .trainingDate(LocalDate.of(2024, 1, 1))
                .trainingDurationMinutes(60)
                .build();

        String toString = t.toString();
        assertTrue(toString.contains("Training{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("trainingName='Morning Cardio'"));
        assertTrue(toString.contains("trainingType=CARDIO"));
        assertTrue(toString.contains("trainingDate=2024-01-01"));
        assertTrue(toString.contains("trainingDurationMinutes=60"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Training t = new Training();
        t.setId(5L);
        t.setTrainingName("Evening Strength");
        t.setTrainingType(TrainingTypeName.STRENGTH);
        t.setTrainingDate(LocalDate.of(2024, 3, 3));
        t.setTrainingDurationMinutes(45);

        assertEquals(5L, t.getId());
        assertEquals("Evening Strength", t.getTrainingName());
        assertEquals(TrainingTypeName.STRENGTH, t.getTrainingType());
        assertEquals(LocalDate.of(2024, 3, 3), t.getTrainingDate());
        assertEquals(45, t.getTrainingDurationMinutes());
    }

    @Test
    void testBuilderWithTraineeAndTrainer() {
        Trainee trainee = Trainee.builder().id(10L).build();
        Trainer trainer = Trainer.builder().id(20L).build();

        Training t = Training.builder()
                .id(100L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Yoga")
                .trainingType(TrainingTypeName.FLEXIBILITY)
                .trainingDate(LocalDate.of(2024, 4, 4))
                .trainingDurationMinutes(30)
                .build();

        assertEquals(trainee, t.getTrainee());
        assertEquals(trainer, t.getTrainer());
        assertEquals("Yoga", t.getTrainingName());
        assertEquals(TrainingTypeName.FLEXIBILITY, t.getTrainingType());
        assertEquals(LocalDate.of(2024, 4, 4), t.getTrainingDate());
        assertEquals(30, t.getTrainingDurationMinutes());
    }
}