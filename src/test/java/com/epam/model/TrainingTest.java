package com.epam.model;

import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTest {

    @Test
    void trainingBuilder_ShouldCreateTraining() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TrainingType type = TrainingType.builder().trainingTypeName("Fitness").build();

        Training training = Training.builder()
                .trainingId(1L)
                .traineeId(1L)
                .trainerId(1L)
                .trainingName("Morning Fitness")
                .trainingType(type)
                .trainingDate(date)
                .trainingDuration(60)
                .build();

        assertEquals(1L, training.getTrainingId());
        assertEquals(1L, training.getTraineeId());
        assertEquals(1L, training.getTrainerId());
        assertEquals("Morning Fitness", training.getTrainingName());
        assertEquals(type, training.getTrainingType());
        assertEquals(date, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void trainingNoArgsConstructor_ShouldCreateEmptyTraining() {
        Training training = new Training();

        assertNull(training.getTrainingId());
        assertNull(training.getTrainingName());
    }

    @Test
    void trainingAllArgsConstructor_ShouldCreateTraining() {
        LocalDate date = LocalDate.now();
        TrainingType type = new TrainingType();

        Training training = new Training(1L, 1L, 1L, "Yoga", type, date, 45);

        assertEquals(1L, training.getTrainingId());
        assertEquals("Yoga", training.getTrainingName());
        assertEquals(45, training.getTrainingDuration());
    }
}
