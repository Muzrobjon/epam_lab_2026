package com.epam.gym.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationException}.
 */
@DisplayName("ValidationException Tests")
class ValidationExceptionTest {

    private static final String TEST_MESSAGE = "Validation failed";
    private static final String NULL_MESSAGE = null;

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertNotNull(exception);
        assertEquals(TEST_MESSAGE, exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with null message")
    void shouldCreateExceptionWithNullMessage() {
        // When
        ValidationException exception = new ValidationException(NULL_MESSAGE);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with empty message")
    void shouldCreateExceptionWithEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        ValidationException exception = new ValidationException(emptyMessage);

        // Then
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Should be instance of Exception")
    void shouldBeInstanceOfException() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Should be instance of Throwable")
    void shouldBeInstanceOfThrowable() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertInstanceOf(Throwable.class, exception);
    }

    @Test
    @DisplayName("Should preserve message exactly as provided")
    void shouldPreserveMessageExactly() {
        // Given
        String messageWithSpecialChars = "Validation failed: field 'email' must match pattern ^[A-Za-z0-9+_.-]+@(.+)$";

        // When
        ValidationException exception = new ValidationException(messageWithSpecialChars);

        // Then
        assertEquals(messageWithSpecialChars, exception.getMessage());
    }

    @Test
    @DisplayName("Should have no cause when only message is provided")
    void shouldHaveNoCause() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should support stack trace")
    void shouldSupportStackTrace() {
        // When
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should be throwable and catchable")
    void shouldBeThrowableAndCatchable() {
        // Given
        ValidationException exception = new ValidationException(TEST_MESSAGE);

        // When & Then - verify it can be thrown and caught
        try {
            throw exception;
        } catch (ValidationException caught) {
            assertEquals(TEST_MESSAGE, caught.getMessage());
        }
    }

    @Test
    @DisplayName("Should catch as RuntimeException")
    void shouldCatchAsRuntimeException() {
        // Given
        ValidationException thrownException = new ValidationException(TEST_MESSAGE);

        // When & Then
        RuntimeException caught = assertThrows(RuntimeException.class, () -> {
            throw thrownException;
        });
        assertEquals(TEST_MESSAGE, caught.getMessage());
        assertInstanceOf(ValidationException.class, caught);
    }

    @Test
    @DisplayName("Should catch as ValidationException specifically")
    void shouldCatchAsValidationException() {
        // When & Then
        ValidationException caught = assertThrows(ValidationException.class, () -> {
            throw new ValidationException(TEST_MESSAGE);
        });
        assertEquals(TEST_MESSAGE, caught.getMessage());
    }

    @Test
    @DisplayName("Should work with try-catch block")
    void shouldWorkWithTryCatchBlock() {
        // Given
        String caughtMessage;

        // When
        try {
            throw new ValidationException(TEST_MESSAGE);
        } catch (ValidationException e) {
            caughtMessage = e.getMessage();
        }

        // Then
        assertEquals(TEST_MESSAGE, caughtMessage);
    }

    @Test
    @DisplayName("Should allow subclassing")
    void shouldAllowSubclassing() {
        // Given
        class SpecificValidationException extends ValidationException {
            SpecificValidationException() {
                super(ValidationExceptionTest.TEST_MESSAGE);
            }
        }

        // When
        SpecificValidationException exception = new SpecificValidationException();

        // Then
        assertEquals(TEST_MESSAGE, exception.getMessage());
        assertInstanceOf(ValidationException.class, exception);
    }
}