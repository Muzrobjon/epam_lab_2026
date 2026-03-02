package com.epam.gym.service;

import com.epam.gym.model.Trainee;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void generateUsername_shouldReturnNormalizedUsername_whenAvailable() {
        Trainee user = new Trainee();
        user.setFirstName("John");
        user.setLastName("Doe");

        Predicate<String> usernameExists = username -> false;

        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("John.Doe", username);
    }

    @Test
    void generateUsername_shouldAppendNumber_whenUsernameTaken() {
        Trainee user = new Trainee();
        user.setFirstName("Jane");
        user.setLastName("Smith");

        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("Jane.Smith");
        existingUsernames.add("Jane.Smith1");

        Predicate<String> usernameExists = existingUsernames::contains;

        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("Jane.Smith2", username);
    }

    @Test
    void generateUsername_shouldTrimAndRemoveSpaces() {
        Trainee user = new Trainee();
        user.setFirstName("  Alice  ");
        user.setLastName("  Johnson  ");

        Predicate<String> usernameExists = username -> false;

        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("Alice.Johnson", username);
    }

    @Test
    void generateUsername_shouldHandleMultipleCollisions() {
        Trainee user = new Trainee();
        user.setFirstName("Bob");
        user.setLastName("Brown");

        Set<String> existingUsernames = new HashSet<>();
        existingUsernames.add("Bob.Brown");
        existingUsernames.add("Bob.Brown1");
        existingUsernames.add("Bob.Brown2");
        existingUsernames.add("Bob.Brown3");

        Predicate<String> usernameExists = existingUsernames::contains;

        String username = usernameGenerator.generateUsername(user, usernameExists);

        assertEquals("Bob.Brown4", username);
    }
}