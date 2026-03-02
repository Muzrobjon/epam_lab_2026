package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Validation failed";

        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        ValidationException exception = new ValidationException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowValidationException() {
        String message = "Invalid input data";

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> {
                    throw new ValidationException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }
}