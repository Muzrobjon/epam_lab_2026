package com.epam.gym.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessageCorrectly() {
        // Given
        String expectedMessage = "Validation failed";

        // When
        ValidationException exception = new ValidationException(expectedMessage);

        // Then
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_WithNullMessage_ShouldAllowNullMessage() {
        // When
        ValidationException exception = new ValidationException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    void constructor_WithEmptyMessage_ShouldAllowEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        ValidationException exception = new ValidationException(emptyMessage);

        // Then
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void exception_ShouldExtendRuntimeException() {
        // When
        ValidationException exception = new ValidationException("test");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_ShouldBeThrowable() {
        // When
        ValidationException exception = new ValidationException("test");

        // Then
        assertTrue(exception instanceof Throwable);
    }

    @Test
    void exception_ShouldPreserveMessageDetail() {
        // Given
        String detailedMessage = "Field 'email' must be a valid email address";

        // When
        ValidationException exception = new ValidationException(detailedMessage);

        // Then
        assertEquals(detailedMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains("email"));
        assertTrue(exception.getMessage().contains("valid"));
    }

    @Test
    void getCause_ShouldReturnNullWhenNoCauseProvided() {
        // When
        ValidationException exception = new ValidationException("test");

        // Then
        assertNull(exception.getCause());
    }

    @Test
    void exception_CanBeThrownAndCaught() {
        // Given
        String message = "Invalid input";

        // When & Then
        try {
            throw new ValidationException(message);
        } catch (ValidationException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    void exception_CanBeCaughtAsRuntimeException() {
        // Given
        String message = "Invalid input";

        // When & Then
        try {
            throw new ValidationException(message);
        } catch (RuntimeException e) {
            assertEquals(message, e.getMessage());
            assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    void toString_ShouldContainExceptionNameAndMessage() {
        // Given
        String message = "Password is too short";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        String toString = exception.toString();
        assertTrue(toString.contains("ValidationException"));
        assertTrue(toString.contains(message));
    }

    @Test
    void stackTrace_ShouldBePopulatedUponCreation() {
        // When
        ValidationException exception = new ValidationException("test");

        // Then
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void exception_IsUncheckedException() {
        // When & Then - we can throw it without declaring in method signature
        assertThrows(ValidationException.class, () -> {
            throwUnchecked();
        });
    }

    // This helper method does not declare "throws" - proving it's unchecked
    private void throwUnchecked() {
        throw new ValidationException("unchecked");
    }

    @Test
    void exception_CanBeThrownWithoutThrowsDeclaration() {
        // When
        RuntimeException thrown = assertThrows(RuntimeException.class, this::throwUnchecked);

        // Then
        assertTrue(thrown instanceof ValidationException);
        assertEquals("unchecked", thrown.getMessage());
    }

    @Test
    void exception_MessageCanContainFieldNameAndConstraint() {
        // Given
        String fieldName = "username";
        String constraint = "must be between 3 and 20 characters";
        String message = String.format("Field '%s' %s", fieldName, constraint);

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertEquals("Field 'username' must be between 3 and 20 characters", exception.getMessage());
    }

    @Test
    void exception_IsDistinctTypeFromOtherCustomExceptions() {
        // Given
        ValidationException validation = new ValidationException("validation failed");
        NotFoundException notFound = new NotFoundException("not found");
        AuthenticationException auth = new AuthenticationException("auth failed");

        // Then - verify they are different types by checking class equality
        assertNotEquals(validation.getClass(), notFound.getClass());
        assertNotEquals(validation.getClass(), auth.getClass());

        // All extend RuntimeException
        assertTrue(validation instanceof RuntimeException);
        assertTrue(notFound instanceof RuntimeException);
        assertTrue(auth instanceof RuntimeException);
    }

    @Test
    void exception_CatchBlockIsDistinctFromOthers() {
        // Given
        RuntimeException validation = new ValidationException("validation failed");

        // Then - verify catch block behavior
        boolean validationCaught = false;

        try {
            throw validation;
        } catch (ValidationException e) {
            validationCaught = true;
        } catch (NotFoundException | AuthenticationException e) {
            // Should not happen
        }

        assertTrue(validationCaught, "ValidationException should be caught by its own catch block");
    }

    @Test
    void exception_CanBeUsedForMultipleValidationErrors() {
        // Given
        String error1 = "Username is required";
        String error2 = "Email format is invalid";
        String combinedMessage = error1 + "; " + error2;

        // When
        ValidationException exception = new ValidationException(combinedMessage);

        // Then
        assertTrue(exception.getMessage().contains("Username"));
        assertTrue(exception.getMessage().contains("Email"));
    }
}