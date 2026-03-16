package com.epam.gym.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    @DisplayName("Exception should be created with message")
    void shouldCreateExceptionWithMessage() {
        String expectedMessage = "Entity not found";

        NotFoundException exception = new NotFoundException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage(),
                "Exception message should match provided message");
    }

    @Test
    @DisplayName("Exception should extend RuntimeException")
    void shouldExtendRuntimeException() {
        NotFoundException exception = new NotFoundException("test");

        assertInstanceOf(RuntimeException.class, exception, "NotFoundException should extend RuntimeException");
    }

    @Test
    @DisplayName("Exception message should not be null")
    void messageShouldNotBeNull() {
        NotFoundException exception = new NotFoundException("test message");

        assertNotNull(exception.getMessage(),
                "Exception message should not be null");
    }

    @Test
    @DisplayName("Exception should be throwable")
    void shouldBeThrowable() {
        assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException("Not found");
        }, "NotFoundException should be throwable");
    }

    @Test
    @DisplayName("Exception message should contain entity context")
    void messageShouldContainContext() {
        String contextMessage = "Trainee not found: John.Doe";

        NotFoundException exception = new NotFoundException(contextMessage);

        assertTrue(exception.getMessage().contains("Trainee"),
                "Exception message should contain entity type");
        assertTrue(exception.getMessage().contains("John.Doe"),
                "Exception message should contain identifier");
    }

    @Test
    @DisplayName("Exception should preserve exact message")
    void shouldPreserveExactMessage() {
        String exactMessage = "Trainer not found: Alice.Fitness";

        NotFoundException exception = new NotFoundException(exactMessage);

        assertEquals(exactMessage, exception.getMessage(),
                "Exception should preserve exact message without modification");
    }

    @Test
    @DisplayName("Exception type should be RuntimeException")
    void shouldBeUncheckedException() {
        NotFoundException exception = new NotFoundException("test");

        assertInstanceOf(RuntimeException.class, exception, "Should be unchecked exception (RuntimeException)");
    }

    @Test
    @DisplayName("Multiple exceptions should have independent messages")
    void multipleExceptionsShouldHaveIndependentMessages() {
        NotFoundException exception1 = new NotFoundException("User not found");
        NotFoundException exception2 = new NotFoundException("Training not found");

        assertNotEquals(exception1.getMessage(), exception2.getMessage(),
                "Different exceptions should have different messages");
    }

    @Test
    @DisplayName("Exception with empty message should be creatable")
    void shouldAllowEmptyMessage() {
        NotFoundException exception = new NotFoundException("");

        assertEquals("", exception.getMessage(),
                "Exception should allow empty message");
    }

    @Test
    @DisplayName("Exception with null message should be creatable")
    void shouldAllowNullMessage() {
        NotFoundException exception = new NotFoundException(null);

        assertNull(exception.getMessage(),
                "Exception should allow null message (delegates to super)");
    }

    @Test
    @DisplayName("Exception should work with different entity types")
    void shouldWorkWithDifferentEntityTypes() {
        NotFoundException traineeException = new NotFoundException("Trainee not found");
        NotFoundException trainerException = new NotFoundException("Trainer not found");
        NotFoundException trainingException = new NotFoundException("Training not found");

        assertAll("All entity types",
                () -> assertTrue(traineeException.getMessage().contains("Trainee")),
                () -> assertTrue(trainerException.getMessage().contains("Trainer")),
                () -> assertTrue(trainingException.getMessage().contains("Training"))
        );
    }

    @Test
    @DisplayName("Exception can be caught as RuntimeException")
    void canBeCaughtAsRuntimeException() {
        RuntimeException caught;

        try {
            throw new NotFoundException("Test message");
        } catch (RuntimeException e) {
            caught = e;
        }

        assertNotNull(caught);
        assertInstanceOf(NotFoundException.class, caught);
        assertEquals("Test message", caught.getMessage());
    }
}