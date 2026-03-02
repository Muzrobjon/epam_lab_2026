package com.epam.gym.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void testPasswordLength() {
        int length = 12;
        String password = passwordGenerator.generatePassword(length);
        assertNotNull(password, "Password should not be null");
        assertEquals(length, password.length(), "Password length should match requested length");
    }

    @Test
    void testPasswordContainsOnlyValidCharacters() {
        String password = passwordGenerator.generatePassword(50);
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#";
        for (char c : password.toCharArray()) {
            assertTrue(validChars.indexOf(c) >= 0, "Password contains invalid character: " + c);
        }
    }

    @Test
    void testRandomness() {
        String password1 = passwordGenerator.generatePassword(20);
        String password2 = passwordGenerator.generatePassword(20);
        assertNotEquals(password1, password2, "Two generated passwords should not be identical");
    }
}