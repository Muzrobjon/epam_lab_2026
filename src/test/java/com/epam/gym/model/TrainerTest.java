package com.epam.gym.model;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTest {

    @Test
    void testEqualsAndHashCode() {
        Trainer t1 = Trainer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .specialization(TrainingTypeName.CARDIO)
                .build();

        Trainer t2 = Trainer.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .isActive(false)
                .specialization(TrainingTypeName.STRENGTH)
                .build();

        Trainer t3 = Trainer.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .specialization(TrainingTypeName.CARDIO)
                .build();

        assertEquals(t1, t2, "Trainers with same id should be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Hash codes should match for same id");
        assertNotEquals(t1, t3, "Trainers with different ids should not be equal");
    }

    @Test
    void testToString() {
        Trainer t = Trainer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .specialization(TrainingTypeName.CARDIO)
                .build();

        String toString = t.toString();
        assertTrue(toString.contains("Trainer{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("firstName='John'"));
        assertTrue(toString.contains("specialization=CARDIO"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Trainer t = new Trainer();
        t.setId(5L);
        t.setFirstName("Alice");
        t.setLastName("Wonderland");
        t.setUserName("alice");
        t.setIsActive(false);
        t.setSpecialization(TrainingTypeName.STRENGTH);

        assertEquals(5L, t.getId());
        assertEquals("Alice", t.getFirstName());
        assertEquals("Wonderland", t.getLastName());
        assertEquals("alice", t.getUserName());
        assertFalse(t.getIsActive());
        assertEquals(TrainingTypeName.STRENGTH, t.getSpecialization());
    }

    @Test
    void testAddAndRemoveTraining() {
        Trainer trainer = Trainer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .specialization(TrainingTypeName.CARDIO)
                .build();

        Training training = new Training();
        assertTrue(trainer.getTrainings().isEmpty());

        trainer.addTraining(training);
        assertEquals(1, trainer.getTrainings().size());
        assertSame(trainer, training.getTrainer());

        trainer.removeTraining(training);
        assertTrue(trainer.getTrainings().isEmpty());
        assertNull(training.getTrainer());
    }
}