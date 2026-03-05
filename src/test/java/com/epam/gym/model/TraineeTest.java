package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// TODO:
//  In the entire test/model package there is little value in testing Lombok-generated functionality
//  as this logic is already tested by the Lombok library creators:)
//  If the goal was to verify how the entity relationships behave, let's involve the database in the test. For example:
//  create/update/delete entities or modify relations -> save entity -> query it by id -> verify that the relationships
//  were persisted correctly.
class TraineeTest {

    @Test
    void shouldCreateTraineeUsingBuilder() {
        LocalDate birthDate = LocalDate.of(2000, 1, 1);

        Trainee trainee = Trainee.builder()
                .dateOfBirth(birthDate)
                .address("New York")
                .build();

        assertNotNull(trainee);
        assertEquals(birthDate, trainee.getDateOfBirth());
        assertEquals("New York", trainee.getAddress());
        assertNotNull(trainee.getTrainings());
        assertNotNull(trainee.getTrainers());
        assertTrue(trainee.getTrainings().isEmpty());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    @Test
    void shouldAddTrainerBidirectionally() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        trainee.addTrainer(trainer);

        assertTrue(trainee.getTrainers().contains(trainer));
        assertTrue(trainer.getTrainees().contains(trainee));
    }

    @Test
    void shouldNotDuplicateTrainer() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        trainee.addTrainer(trainer);
        trainee.addTrainer(trainer);

        assertEquals(1, trainee.getTrainers().size());
    }

    @Test
    void shouldRemoveTrainerBidirectionally() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        trainee.addTrainer(trainer);
        trainee.removeTrainer(trainer);

        assertFalse(trainee.getTrainers().contains(trainer));
        assertFalse(trainer.getTrainees().contains(trainee));
    }

    @Test
    void shouldAddTrainingAndSetBackReference() {
        Trainee trainee = new Trainee();
        Training training = new Training();

        trainee.addTraining(training);

        assertTrue(trainee.getTrainings().contains(training));
        assertEquals(trainee, training.getTrainee());
    }

    @Test
    void shouldRemoveTrainingAndClearBackReference() {
        Trainee trainee = new Trainee();
        Training training = new Training();

        trainee.addTraining(training);
        trainee.removeTraining(training);

        assertFalse(trainee.getTrainings().contains(training));
        assertNull(training.getTrainee());
    }
}