package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Entity not found";

        NotFoundException exception = new NotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        NotFoundException exception = new NotFoundException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowNotFoundException() {
        String message = "User not found";

        NotFoundException thrown = assertThrows(
                NotFoundException.class,
                () -> {
                    throw new NotFoundException(message);
                }
        );

        assertEquals(message, thrown.getMessage());
    }
}