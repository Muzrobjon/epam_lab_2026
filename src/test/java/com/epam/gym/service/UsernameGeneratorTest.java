package com.epam.gym.service;

import com.epam.gym.model.User;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    static class TestUser extends User {}

    @Test
    void testGenerateUsername_Basic() {
        TestUser user = new TestUser();
        user.setFirstName("John");
        user.setLastName("Doe");

        Predicate<String> usernameExists = name -> false;
        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("John.Doe", username);
    }

    @Test
    void testGenerateUsername_WithWhitespace() {
        TestUser user = new TestUser();
        user.setFirstName("  John  ");
        user.setLastName("  Doe  ");

        Predicate<String> usernameExists = name -> false;
        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("John.Doe", username);
    }

    @Test
    void testGenerateUsername_Uniqueness() {
        TestUser user = new TestUser();
        user.setFirstName("Jane");
        user.setLastName("Smith");

        Set<String> existing = new HashSet<>();
        existing.add("Jane.Smith");
        existing.add("Jane.Smith1");

        Predicate<String> usernameExists = existing::contains;
        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("Jane.Smith2", username);
    }

    @Test
    void testGenerateUsername_EmptyNames() {
        TestUser user = new TestUser();
        user.setFirstName(" ");
        user.setLastName(" ");

        Predicate<String> usernameExists = name -> false;
        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals(".", username);
    }
}