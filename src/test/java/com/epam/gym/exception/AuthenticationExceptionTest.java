package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String errorMessage = "Invalid username or password";

        AuthenticationException exception =
                new AuthenticationException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        AuthenticationException exception =
                new AuthenticationException("Test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowAuthenticationException() {
        String errorMessage = "Access denied";

        AuthenticationException thrown = assertThrows(
                AuthenticationException.class,
                () -> {
                    throw new AuthenticationException(errorMessage);
                }
        );

        assertEquals(errorMessage, thrown.getMessage());
    }
}