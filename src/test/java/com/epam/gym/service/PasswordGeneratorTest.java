package com.epam.gym.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void testGeneratePasswordLength() {
        int length = 12;
        String password = passwordGenerator.generatePassword(length);
        assertNotNull(password);
        assertEquals(length, password.length());
    }

    @Test
    void testGeneratePasswordAllowedCharacters() {
        int length = 20;
        String password = passwordGenerator.generatePassword(length);
        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#";
        for (char c : password.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0, "Password contains invalid character: " + c);
        }
    }

    @Test
    void testGeneratePasswordRandomness() {
        String password1 = passwordGenerator.generatePassword(10);
        String password2 = passwordGenerator.generatePassword(10);
        // It's possible (but extremely unlikely) for two random passwords to be equal
        assertNotEquals(password1, password2, "Two generated passwords should not be equal");
    }

    @Test
    void testGeneratePasswordZeroLength() {
        String password = passwordGenerator.generatePassword(0);
        assertEquals("", password);
    }

    @Test
    void testGeneratePasswordNegativeLength() {
        assertThrows(NegativeArraySizeException.class, () -> passwordGenerator.generatePassword(-1));
    }
}