package com.epam.model;

import com.epam.gym.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userBuilder_ShouldCreateUser() {
        User user = User.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .isActive(true)
                .build();

        assertEquals(1L, user.getUserId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe", user.getUserName());
        assertTrue(user.isActive());
    }

    @Test
    void userNoArgsConstructor_ShouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getUserId());
        assertNull(user.getFirstName());
    }

    @Test
    void userAllArgsConstructor_ShouldCreateUser() {
        User user = new User(1L, "Jane", "Smith", "jane.smith", false);

        assertEquals(1L, user.getUserId());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("jane.smith", user.getUserName());
        assertFalse(user.isActive());
    }

    @Test
    void userSettersAndGetters_ShouldWork() {
        User user = new User();
        user.setUserId(2L);
        user.setFirstName("Mike");
        user.setLastName("Johnson");
        user.setUserName("mike.johnson");
        user.setActive(true);

        assertEquals(2L, user.getUserId());
        assertEquals("Mike", user.getFirstName());
        assertTrue(user.isActive());
    }
}
