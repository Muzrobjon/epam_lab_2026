package com.epam.service;

import com.epam.gym.service.PasswordGenerator;
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
    void generatePassword_ShouldReturnPasswordOfCorrectLength() {
        // Arrange
        int length = 12;

        // Act
        String password = passwordGenerator.generatePassword(length);

        // Assert
        assertNotNull(password, "Generated password should not be null");
        assertEquals(length, password.length(), "Password length should be " + length);
    }

    @Test
    void generatePassword_ShouldOnlyContainValidCharacters() {
        // Arrange
        int length = 10;

        // Act
        String password = passwordGenerator.generatePassword(length);

        // Assert
        assertNotNull(password, "Generated password should not be null");
        for (char c : password.toCharArray()) {
            assertTrue("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#".indexOf(c) >= 0,
                    "Password contains invalid character: " + c);
        }
    }

    @Test
    void generatePassword_ShouldGenerateDifferentPasswords() {
        // Arrange
        int length = 8;

        // Act
        String password1 = passwordGenerator.generatePassword(length);
        String password2 = passwordGenerator.generatePassword(length);

        // Assert
        assertNotNull(password1, "First generated password should not be null");
        assertNotNull(password2, "Second generated password should not be null");
        assertNotEquals(password1, password2, "Generated passwords should be different (randomness test)");
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithinAllowedCharacterSet() {
        // Arrange
        int length = 15;
        String password = passwordGenerator.generatePassword(length);

        // Act & Assert
        assertNotNull(password, "Generated password should not be null");
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#";
        for (char c : password.toCharArray()) {
            assertTrue(allowedChars.indexOf(c) >= 0, "Password contains invalid character: " + c);
        }
    }
}
