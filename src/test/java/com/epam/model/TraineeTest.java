package com.epam.model;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Training;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TraineeTest {

    @Test
    void traineeBuilder_ShouldCreateTrainee() {
        LocalDate dob = LocalDate.of(1990, 5, 15);
        List<Training> trainings = new ArrayList<>();

        Trainee trainee = Trainee.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .isActive(true)
                .dateOfBirth(dob)
                .address("123 Main St")
                .trainings(trainings)
                .build();

        assertEquals(1L, trainee.getUserId());
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals("john.doe", trainee.getUserName());
        assertTrue(trainee.isActive());
        assertEquals(dob, trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
        assertEquals(trainings, trainee.getTrainings());
    }

    @Test
    void traineeBuilder_DefaultTrainings_ShouldInitializeEmptyList() {
        Trainee trainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertNotNull(trainee.getTrainings());
        assertTrue(trainee.getTrainings().isEmpty());
    }

    @Test
    void traineeInheritance_ShouldHaveUserFields() {
        Trainee trainee = new Trainee();
        trainee.setUserId(1L);
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUserName("john.doe");
        trainee.setActive(true);

        assertEquals(1L, trainee.getUserId());
        assertEquals("John", trainee.getFirstName());
    }

    @Test
    void traineeEqualsAndHashCode_ShouldWorkCorrectly() {
        Trainee trainee1 = Trainee.builder().userId(1L).firstName("John").build();
        Trainee trainee2 = Trainee.builder().userId(1L).firstName("John").build();
        Trainee trainee3 = Trainee.builder().userId(2L).firstName("Jane").build();

        assertEquals(trainee1, trainee2);
        assertEquals(trainee1.hashCode(), trainee2.hashCode());
        assertNotEquals(trainee1, trainee3);
    }

    @Test
    void traineeToString_ShouldContainAllFields() {
        Trainee trainee = Trainee.builder()
                .userId(1L)
                .firstName("John")
                .build();

        String toString = trainee.toString();
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("userId=1"));
    }
}
