package com.epam.gym.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationExceptionTest {

    @Test
    @DisplayName("Exception should be created with message")
    void shouldCreateExceptionWithMessage() {
        String expectedMessage = "Invalid credentials";

        AuthenticationException exception = new AuthenticationException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should match provided message");
    }

    @Test
    @DisplayName("Exception should extend RuntimeException")
    void shouldExtendRuntimeException() {
        AuthenticationException exception = new AuthenticationException("test");

        assertInstanceOf(RuntimeException.class, exception, "AuthenticationException should extend RuntimeException");
    }

    @Test
    @DisplayName("Exception message should not be null")
    void messageShouldNotBeNull() {
        AuthenticationException exception = new AuthenticationException("test message");

        assertNotNull(exception.getMessage(),
                "Exception message should not be null");
    }

    @Test
    @DisplayName("Exception should be throwable")
    void shouldBeThrowable() {
        assertThrows(AuthenticationException.class, () -> {
            throw new AuthenticationException("Authentication failed");
        }, "AuthenticationException should be throwable");
    }

    @Test
    @DisplayName("Exception message should contain authentication context")
    void messageShouldContainContext() {
        String contextMessage = "User not found: John.Doe";

        AuthenticationException exception = new AuthenticationException(contextMessage);

        assertTrue(exception.getMessage().contains("John.Doe"),
                "Exception message should contain user context");
    }

    @Test
    @DisplayName("Exception should preserve exact message")
    void shouldPreserveExactMessage() {
        String exactMessage = "Invalid password for user: admin";

        AuthenticationException exception = new AuthenticationException(exactMessage);

        assertEquals(exactMessage, exception.getMessage(),
                "Exception should preserve exact message without modification");
    }

    @Test
    @DisplayName("Exception type should be RuntimeException")
    void shouldBeUncheckedException() {
        AuthenticationException exception = new AuthenticationException("test");

        assertInstanceOf(RuntimeException.class, exception, "Should be unchecked exception (RuntimeException)");
        assertFalse(false,
                "Should not be checked exception");
    }

    @Test
    @DisplayName("Multiple exceptions should have independent messages")
    void multipleExceptionsShouldHaveIndependentMessages() {
        AuthenticationException exception1 = new AuthenticationException("First error");
        AuthenticationException exception2 = new AuthenticationException("Second error");

        assertNotEquals(exception1.getMessage(), exception2.getMessage(),
                "Different exceptions should have different messages");
        assertEquals("First error", exception1.getMessage());
        assertEquals("Second error", exception2.getMessage());
    }

    @Test
    @DisplayName("Exception with empty message should be creatable")
    void shouldAllowEmptyMessage() {
        AuthenticationException exception = new AuthenticationException("");

        assertEquals("", exception.getMessage(),
                "Exception should allow empty message");
    }

    @Test
    @DisplayName("Exception with null message should be creatable")
    void shouldAllowNullMessage() {
        AuthenticationException exception = new AuthenticationException(null);

        assertNull(exception.getMessage(),
                "Exception should allow null message (delegates to super)");
    }
}