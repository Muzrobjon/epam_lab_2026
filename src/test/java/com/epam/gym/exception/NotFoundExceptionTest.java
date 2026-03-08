package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessageCorrectly() {
        // Given
        String expectedMessage = "Resource not found";

        // When
        NotFoundException exception = new NotFoundException(expectedMessage);

        // Then
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_WithNullMessage_ShouldAllowNullMessage() {
        // When
        NotFoundException exception = new NotFoundException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldAllowEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        NotFoundException exception = new NotFoundException(emptyMessage);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void exception_ShouldExtendRuntimeException() {
        // When
        NotFoundException exception = new NotFoundException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_ShouldBeThrowable() {
        // When
        NotFoundException exception = new NotFoundException("test");

        // Then
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void exception_ShouldPreserveMessageDetail() {
        // Given
        String detailedMessage = "User with id '12345' not found in database";

        // When
        NotFoundException exception = new NotFoundException(detailedMessage);

        // Then
        assertEquals(detailedMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("12345"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void getCause_ShouldReturnNullWhenNoCauseProvided() {
        // When
        NotFoundException exception = new NotFoundException("test");

        // Then
        assertNull(exception.getCause());
    }

    @Test
    void exception_CanBeThrownAndCaught() {
        // Given
        String message = "Entity not found";

        // When & Then
        try {
            throw new NotFoundException(message);
        } catch (NotFoundException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    void exception_CanBeCaughtAsRuntimeException() {
        // Given
        String message = "Entity not found";

        // When & Then
        try {
            throw new NotFoundException(message);
        } catch (RuntimeException e) {
            assertEquals(message, e.getMessage());
            assertTrue(e instanceof NotFoundException);
        }
    }

    @Test
    void toString_ShouldContainExceptionNameAndMessage() {
        // Given
        String message = "Training not found";

        // When
        NotFoundException exception = new NotFoundException(message);

        // Then
        String toString = exception.toString();
        assertTrue(toString.contains("NotFoundException"));
        assertTrue(toString.contains(message));
    }

    @Test
    void stackTrace_ShouldBePopulatedUponCreation() {
        // When
        NotFoundException exception = new NotFoundException("test");

        // Then
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void exception_IsUncheckedException() {
        // When & Then - we can throw it without declaring in method signature
        assertThrows(NotFoundException.class, () -> {
            throwUnchecked();
        });
    }

    // This helper method does not declare "throws" - proving it's unchecked
    private void throwUnchecked() {
        throw new NotFoundException("unchecked");
    }

    @Test
    void exception_CanBeThrownWithoutThrowsDeclaration() {
        // When
        RuntimeException thrown = assertThrows(RuntimeException.class, this::throwUnchecked);

        // Then
        assertTrue(thrown instanceof NotFoundException);
        assertEquals("unchecked", thrown.getMessage());
    }

    @Test
    void exception_MessageCanContainEntityTypeAndIdentifier() {
        // Given
        String entityType = "Trainer";
        String identifier = "john.doe";
        String message = String.format("%s with username '%s' not found", entityType, identifier);

        // When
        NotFoundException exception = new NotFoundException(message);

        // Then
        assertEquals("Trainer with username 'john.doe' not found", exception.getMessage());
    }

    @Test
    void exception_IsDistinctTypeFromAuthenticationException() {
        // Given
        NotFoundException notFound = new NotFoundException("not found");
        AuthenticationException auth = new AuthenticationException("auth failed");

        // Then - verify they are different types by checking class equality
        assertNotEquals(notFound.getClass(), auth.getClass());
        assertEquals(NotFoundException.class, notFound.getClass());
        assertEquals(AuthenticationException.class, auth.getClass());

        // Both extend RuntimeException but are not assignable to each other
        assertTrue(notFound instanceof RuntimeException);
        assertTrue(auth instanceof RuntimeException);
    }

    @Test
    void exception_CatchBlocksAreDistinct() {
        // Given
        RuntimeException notFound = new NotFoundException("not found");
        RuntimeException auth = new AuthenticationException("auth failed");

        // Then - verify catch block behavior
        boolean notFoundCaughtAsNotFound = false;
        boolean authCaughtAsAuth = false;

        try {
            throw notFound;
        } catch (NotFoundException e) {
            notFoundCaughtAsNotFound = true;
        } catch (AuthenticationException e) {
            // Should not happen
        }

        try {
            throw auth;
        } catch (AuthenticationException e) {
            authCaughtAsAuth = true;
        } catch (NotFoundException e) {
            // Should not happen
        }

        assertTrue(notFoundCaughtAsNotFound, "NotFoundException should be caught by its own catch block");
        assertTrue(authCaughtAsAuth, "AuthenticationException should be caught by its own catch block");
    }
}