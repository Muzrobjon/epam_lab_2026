package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TraineeTest {

    @Test
    void testEqualsAndHashCode() {
        Trainee t1 = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Address 1")
                .build();

        Trainee t2 = Trainee.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .isActive(false)
                .dateOfBirth(LocalDate.of(1999, 2, 2))
                .address("Address 2")
                .build();

        Trainee t3 = Trainee.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Address 1")
                .build();

        assertEquals(t1, t2, "Trainees with same id should be equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Hash codes should match for same id");
        assertNotEquals(t1, t3, "Trainees with different ids should not be equal");
    }

    @Test
    void testToString() {
        Trainee t = Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .isActive(true)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Address 1")
                .build();

        String toString = t.toString();
        assertTrue(toString.contains("Trainee{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("firstName='John'"));
        assertTrue(toString.contains("dateOfBirth=2000-01-01"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        Trainee t = new Trainee();
        t.setId(5L);
        t.setFirstName("Alice");
        t.setLastName("Wonderland");
        t.setUserName("alice");
        t.setIsActive(false);
        t.setDateOfBirth(LocalDate.of(1995, 5, 5));
        t.setAddress("Wonderland");

        assertEquals(5L, t.getId());
        assertEquals("Alice", t.getFirstName());
        assertEquals("Wonderland", t.getLastName());
        assertEquals("alice", t.getUserName());
        assertFalse(t.getIsActive());
        assertEquals(LocalDate.of(1995, 5, 5), t.getDateOfBirth());
        assertEquals("Wonderland", t.getAddress());
    }
}