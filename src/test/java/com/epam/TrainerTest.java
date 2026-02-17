package com.epam;

import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTest {

    @Test
    void trainerBuilder_ShouldCreateTrainer() {
        List<Training> trainings = new ArrayList<>();

        Trainer trainer = Trainer.builder()
                .userId(1L)
                .firstName("Mike")
                .lastName("Johnson")
                .userName("mike.johnson")
                .isActive(true)
                .specialization("Fitness")
                .trainings(trainings)
                .build();

        assertEquals(1L, trainer.getUserId());
        assertEquals("Mike", trainer.getFirstName());
        assertEquals("Johnson", trainer.getLastName());
        assertEquals("mike.johnson", trainer.getUserName());
        assertTrue(trainer.isActive());
        assertEquals("Fitness", trainer.getSpecialization());
        assertEquals(trainings, trainer.getTrainings());
    }

    @Test
    void trainerBuilder_DefaultTrainings_ShouldInitializeEmptyList() {
        Trainer trainer = Trainer.builder()
                .firstName("Mike")
                .lastName("Johnson")
                .build();

        assertNotNull(trainer.getTrainings());
        assertTrue(trainer.getTrainings().isEmpty());
    }

    @Test
    void trainerInheritance_ShouldHaveUserFields() {
        Trainer trainer = new Trainer();
        trainer.setUserId(1L);
        trainer.setFirstName("Mike");
        trainer.setSpecialization("Yoga");

        assertEquals(1L, trainer.getUserId());
        assertEquals("Yoga", trainer.getSpecialization());
    }

    @Test
    void trainerEqualsAndHashCode_ShouldWorkCorrectly() {
        Trainer trainer1 = Trainer.builder().userId(1L).specialization("Fitness").build();
        Trainer trainer2 = Trainer.builder().userId(1L).specialization("Fitness").build();
        Trainer trainer3 = Trainer.builder().userId(2L).specialization("Yoga").build();

        assertEquals(trainer1, trainer2);
        assertEquals(trainer1.hashCode(), trainer2.hashCode());
        assertNotEquals(trainer1, trainer3);
    }
}
