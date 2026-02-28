package com.epam.gym.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userBuilder_ShouldCreateUserWithCorrectValues() {
        // Create a User object using the builder pattern
        User user = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        // Assert the values are correctly set via builder
        assertNotNull(user);
        assertEquals(1L, user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe", user.getUserName());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isActive());
    }

    @Test
    void userNoArgsConstructor_ShouldCreateEmptyUser() {
        // Test the no-args constructor
        User user = new User();

        // Assert default values for no-args constructor
        assertNull(user.getUserId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getUserName());
        assertNull(user.getPassword());
        assertFalse(user.isActive());
    }

    @Test
    void userAllArgsConstructor_ShouldCreateUserWithValues() {
        // Test the all-args constructor
        User user = new User(1L, "John", "Doe", "john.doe", "password123", true);

        // Assert that the user is correctly created with the provided values
        assertNotNull(user);
        assertEquals(1L, user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe", user.getUserName());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isActive());
    }

    @Test
    void userEquals_ShouldReturnTrueForSameData() {
        // Create two identical User objects using builder
        User user1 = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        User user2 = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        // Assert that the two users are equal because their data is identical
        assertEquals(user1, user2);
    }

    @Test
    void userHashCode_ShouldReturnSameHashCodeForSameData() {
        // Create two identical User objects using builder
        User user1 = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        User user2 = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        // Assert that the hash codes of the two identical users are the same
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void userToString_ShouldContainAllAttributes() {
        // Create a User object using builder
        User user = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        // Assert that the toString contains expected attributes
        String expectedString = "User(userId=1, firstName=John, lastName=Doe, userName=john.doe, password=password123, isActive=true)";
        assertTrue(user.toString().contains("userId=1"));
        assertTrue(user.toString().contains("firstName=John"));
        assertTrue(user.toString().contains("lastName=Doe"));
        assertTrue(user.toString().contains("userName=john.doe"));
        assertTrue(user.toString().contains("password=password123"));
        assertTrue(user.toString().contains("isActive=true"));
    }
}
