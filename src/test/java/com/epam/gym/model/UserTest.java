package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateTraineeWithUserFields() {
        Trainee trainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("secret")
                .build();

        assertNotNull(trainee);
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals("johndoe", trainee.getUserName());
        assertEquals("secret", trainee.getPassword());
        assertTrue(trainee.getIsActive()); // default
    }

    @Test
    void shouldSetAndGetUserFields() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Alice");
        trainer.setLastName("Smith");
        trainer.setUserName("alice123");
        trainer.setPassword("pass123");
        trainer.setIsActive(false);

        assertEquals("Alice", trainer.getFirstName());
        assertEquals("Smith", trainer.getLastName());
        assertEquals("alice123", trainer.getUserName());
        assertEquals("pass123", trainer.getPassword());
        assertFalse(trainer.getIsActive());
    }
}