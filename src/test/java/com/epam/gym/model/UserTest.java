package com.epam.gym.model;

import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @SuperBuilder
    @NoArgsConstructor
    static class TestUser extends User {}

    @Test
    void testEqualsAndHashCode() {
        TestUser u1 = TestUser.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("pass")
                .isActive(true)
                .build();

        TestUser u2 = TestUser.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .userName("janesmith")
                .password("pass2")
                .isActive(false)
                .build();

        TestUser u3 = TestUser.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("pass")
                .isActive(true)
                .build();

        assertEquals(u1, u2, "Users with same id should be equal");
        assertEquals(u1.hashCode(), u2.hashCode(), "Hash codes should match for same id");
        assertNotEquals(u1, u3, "Users with different ids should not be equal");
    }

    @Test
    void testToString() {
        TestUser u = TestUser.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("johndoe")
                .password("pass")
                .isActive(true)
                .build();

        String toString = u.toString();
        assertTrue(toString.contains("User{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("firstName='John'"));
        assertTrue(toString.contains("lastName='Doe'"));
        assertTrue(toString.contains("userName='johndoe'"));
        assertTrue(toString.contains("isActive=true"));
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        TestUser u = new TestUser();
        u.setId(5L);
        u.setFirstName("Alice");
        u.setLastName("Wonderland");
        u.setUserName("alice");
        u.setPassword("secret");
        u.setIsActive(false);

        assertEquals(5L, u.getId());
        assertEquals("Alice", u.getFirstName());
        assertEquals("Wonderland", u.getLastName());
        assertEquals("alice", u.getUserName());
        assertEquals("secret", u.getPassword());
        assertFalse(u.getIsActive());
    }
}