package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTest {

    @Test
    void shouldCreateTrainerUsingBuilder() {
        TrainingType specialization = new TrainingType();

        Trainer trainer = Trainer.builder()
                .specialization(specialization)
                .build();

        assertNotNull(trainer);
        assertEquals(specialization, trainer.getSpecialization());
        assertNotNull(trainer.getTrainings());
        assertNotNull(trainer.getTrainees());
        assertTrue(trainer.getTrainings().isEmpty());
        assertTrue(trainer.getTrainees().isEmpty());
    }

    @Test
    void shouldAddTrainingAndSetBackReference() {
        Trainer trainer = new Trainer();
        Training training = new Training();

        trainer.addTraining(training);

        assertTrue(trainer.getTrainings().contains(training));
        assertEquals(trainer, training.getTrainer());
    }

    @Test
    void shouldRemoveTrainingAndClearBackReference() {
        Trainer trainer = new Trainer();
        Training training = new Training();

        trainer.addTraining(training);
        trainer.removeTraining(training);

        assertFalse(trainer.getTrainings().contains(training));
        assertNull(training.getTrainer());
    }

    @Test
    void shouldSetAndGetSpecialization() {
        TrainingType specialization = new TrainingType();
        Trainer trainer = new Trainer();

        trainer.setSpecialization(specialization);

        assertEquals(specialization, trainer.getSpecialization());
    }
}