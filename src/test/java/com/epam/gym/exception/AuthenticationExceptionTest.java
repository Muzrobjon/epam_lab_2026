package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessageCorrectly() {
        // Given
        String expectedMessage = "Invalid credentials";

        // When
        AuthenticationException exception = new AuthenticationException(expectedMessage);

        // Then
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_WithNullMessage_ShouldAllowNullMessage() {
        // When
        AuthenticationException exception = new AuthenticationException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldAllowEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        AuthenticationException exception = new AuthenticationException(emptyMessage);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void exception_ShouldExtendRuntimeException() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_ShouldBeThrowable() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void exception_ShouldPreserveMessageDetail() {
        // Given
        String detailedMessage = "User 'john_doe' authentication failed: password mismatch";

        // When
        AuthenticationException exception = new AuthenticationException(detailedMessage);

        // Then
        assertEquals(detailedMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("john_doe"));
        assertTrue(exception.getMessage().contains("authentication failed"));
    }

    @Test
    void getCause_ShouldReturnNullWhenNoCauseProvided() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        assertNull(exception.getCause());
    }

    @Test
    void exception_CanBeThrownAndCaught() {
        // Given
        String message = "Authentication failed";

        // When & Then
        try {
            throw new AuthenticationException(message);
        } catch (AuthenticationException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    void exception_CanBeCaughtAsRuntimeException() {
        // Given
        String message = "Authentication failed";

        // When & Then
        try {
            throw new AuthenticationException(message);
        } catch (RuntimeException e) {
            assertEquals(message, e.getMessage());
            assertTrue(e instanceof AuthenticationException);
        }
    }

    @Test
    void toString_ShouldContainExceptionNameAndMessage() {
        // Given
        String message = "Invalid token";

        // When
        AuthenticationException exception = new AuthenticationException(message);

        // Then
        String toString = exception.toString();
        assertTrue(toString.contains("AuthenticationException"));
        assertTrue(toString.contains(message));
    }

    @Test
    void stackTrace_ShouldBePopulatedUponCreation() {
        // When
        AuthenticationException exception = new AuthenticationException("test");

        // Then
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void exception_IsUncheckedException() {
        // This test verifies that the exception is unchecked by the fact that
        // this method compiles without declaring "throws AuthenticationException"
        // If it were checked, the compiler would force us to declare it or catch it

        // When & Then - we can throw it without declaring in method signature
        assertThrows(AuthenticationException.class, () -> {
            throwUnchecked();
        });
    }

    // This helper method does not declare "throws" - proving it's unchecked
    private void throwUnchecked() {
        throw new AuthenticationException("unchecked");
    }

    @Test
    void exception_CanBeThrownWithoutThrowsDeclaration() {
        // This method itself proves the exception is unchecked - no throws needed

        // When
        RuntimeException thrown = assertThrows(RuntimeException.class, this::throwUnchecked);

        // Then
        assertTrue(thrown instanceof AuthenticationException);
        assertEquals("unchecked", thrown.getMessage());
    }
}