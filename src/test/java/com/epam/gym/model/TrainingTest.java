package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTest {

    @Test
    void shouldCreateTrainingUsingBuilder() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType();
        LocalDate date = LocalDate.of(2024, 3, 1);

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Workout")
                .trainingType(type)
                .trainingDate(date)
                .trainingDurationMinutes(60)
                .build();

        assertNotNull(training);
        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals(type, training.getTrainingType());
        assertEquals("Morning Workout", training.getTrainingName());
        assertEquals(date, training.getTrainingDate());
        assertEquals(60, training.getTrainingDurationMinutes());
    }

    @Test
    void shouldSetAndGetProperties() {
        Training training = new Training();
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType();
        LocalDate date = LocalDate.now();

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Evening Cardio");
        training.setTrainingType(type);
        training.setTrainingDate(date);
        training.setTrainingDurationMinutes(45);

        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals("Evening Cardio", training.getTrainingName());
        assertEquals(type, training.getTrainingType());
        assertEquals(date, training.getTrainingDate());
        assertEquals(45, training.getTrainingDurationMinutes());
    }
}