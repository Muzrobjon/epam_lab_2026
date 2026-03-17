package com.epam.gym.exception;

import com.epam.gym.dto.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    // ==================== Builder Tests ====================

    @Test
    @DisplayName("Should build ErrorResponse using builder with all fields")
    void shouldBuildWithAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ErrorResponse response = ErrorResponse.builder()
                .transactionId("txn-123-abc")
                .message("User not found")
                .status(404)
                .timestamp(now)
                .path("/api/users/123")
                .build();

        // Then
        assertAll("All fields should be set correctly",
                () -> assertEquals("txn-123-abc", response.getTransactionId()),
                () -> assertEquals("User not found", response.getMessage()),
                () -> assertEquals(404, response.getStatus()),
                () -> assertEquals(now, response.getTimestamp()),
                () -> assertEquals("/api/users/123", response.getPath())
        );
    }

    @Test
    @DisplayName("Should build ErrorResponse using builder with partial fields")
    void shouldBuildWithPartialFields() {
        // When
        ErrorResponse response = ErrorResponse.builder()
                .message("Bad request")
                .status(400)
                .build();

        // Then
        assertAll("Partial fields should be set, others null",
                () -> assertNull(response.getTransactionId()),
                () -> assertEquals("Bad request", response.getMessage()),
                () -> assertEquals(400, response.getStatus()),
                () -> assertNull(response.getTimestamp()),
                () -> assertNull(response.getPath())
        );
    }

    // ==================== No-Args Constructor Tests ====================

    @Test
    @DisplayName("Should create empty ErrorResponse using no-args constructor")
    void shouldCreateEmptyResponse() {
        // When
        ErrorResponse response = new ErrorResponse();

        // Then
        assertAll("All fields should be null or default",
                () -> assertNull(response.getTransactionId()),
                () -> assertNull(response.getMessage()),
                () -> assertEquals(0, response.getStatus()),
                () -> assertNull(response.getTimestamp()),
                () -> assertNull(response.getPath())
        );
    }

    // ==================== All-Args Constructor Tests ====================

    @Test
    @DisplayName("Should create ErrorResponse using all-args constructor")
    void shouldCreateWithAllArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ErrorResponse response = new ErrorResponse(
                "txn-456-def",
                "Server error",
                500,
                now,
                "/api/error"
        );

        // Then
        assertAll("All fields should be set via constructor",
                () -> assertEquals("txn-456-def", response.getTransactionId()),
                () -> assertEquals("Server error", response.getMessage()),
                () -> assertEquals(500, response.getStatus()),
                () -> assertEquals(now, response.getTimestamp()),
                () -> assertEquals("/api/error", response.getPath())
        );
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    @DisplayName("Should set and get all fields using setters")
    void shouldSetAndGetAllFields() {
        // Given
        ErrorResponse response = new ErrorResponse();
        LocalDateTime now = LocalDateTime.now();

        // When
        response.setTransactionId("txn-789-ghi");
        response.setMessage("Validation failed");
        response.setStatus(422);
        response.setTimestamp(now);
        response.setPath("/api/validate");

        // Then
        assertAll("All fields should be accessible via getters",
                () -> assertEquals("txn-789-ghi", response.getTransactionId()),
                () -> assertEquals("Validation failed", response.getMessage()),
                () -> assertEquals(422, response.getStatus()),
                () -> assertEquals(now, response.getTimestamp()),
                () -> assertEquals("/api/validate", response.getPath())
        );
    }

    // ==================== Equals and HashCode Tests ====================

    @Test
    @DisplayName("Should consider two ErrorResponses equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        ErrorResponse response1 = ErrorResponse.builder()
                .transactionId("txn-123")
                .message("Error")
                .status(500)
                .timestamp(now)
                .path("/api/test")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .transactionId("txn-123")
                .message("Error")
                .status(500)
                .timestamp(now)
                .path("/api/test")
                .build();

        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Should not consider ErrorResponses equal when fields differ")
    void shouldNotBeEqualWhenFieldsDiffer() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        ErrorResponse response1 = ErrorResponse.builder()
                .transactionId("txn-123")
                .message("Error")
                .status(500)
                .timestamp(now)
                .path("/api/test")
                .build();

        ErrorResponse response2 = ErrorResponse.builder()
                .transactionId("txn-456") // Different
                .message("Error")
                .status(500)
                .timestamp(now)
                .path("/api/test")
                .build();

        // Then
        assertNotEquals(response1, response2);
    }

    @Test
    @DisplayName("Should not be equal to null or different type")
    void shouldNotBeEqualToNullOrDifferentType() {
        // Given
        ErrorResponse response = ErrorResponse.builder()
                .message("Error")
                .build();

        // Then
        assertNotEquals(null, response);
        assertNotEquals("Error", response);
        assertNotEquals(response, new Object());
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("ToString should contain all field values")
    void toStringShouldContainAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response = ErrorResponse.builder()
                .transactionId("txn-abc")
                .message("Test error")
                .status(400)
                .timestamp(now)
                .path("/api/test")
                .build();

        // When
        String toString = response.toString();

        // Then
        assertAll("ToString should contain field information",
                () -> assertTrue(toString.contains("txn-abc")),
                () -> assertTrue(toString.contains("Test error")),
                () -> assertTrue(toString.contains("400")),
                () -> assertTrue(toString.contains(now.toString())),
                () -> assertTrue(toString.contains("/api/test"))
        );
    }

    // ==================== Data Annotation Tests ====================

    @Test
    @DisplayName("Should support fluent setter style (returns void, not this)")
    void shouldSupportStandardSetters() {
        // Given
        ErrorResponse response = new ErrorResponse();

        // When - Standard setters return void, not this (unlike @Accessors(chain=true))
        response.setTransactionId("txn-fluent");
        response.setStatus(200);

        // Then
        assertEquals("txn-fluent", response.getTransactionId());
        assertEquals(200, response.getStatus());
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should handle null values in all fields")
    void shouldHandleNullValues() {
        // When
        ErrorResponse response = ErrorResponse.builder()
                .transactionId(null)
                .message(null)
                .status(0)
                .timestamp(null)
                .path(null)
                .build();

        // Then
        assertAll("Null values should be allowed",
                () -> assertNull(response.getTransactionId()),
                () -> assertNull(response.getMessage()),
                () -> assertEquals(0, response.getStatus()),
                () -> assertNull(response.getTimestamp()),
                () -> assertNull(response.getPath())
        );
    }

    @Test
    @DisplayName("Should handle empty string values")
    void shouldHandleEmptyStrings() {
        // When
        ErrorResponse response = ErrorResponse.builder()
                .transactionId("")
                .message("")
                .path("")
                .status(204)
                .timestamp(LocalDateTime.now())
                .build();

        // Then
        assertAll("Empty strings should be allowed",
                () -> assertEquals("", response.getTransactionId()),
                () -> assertEquals("", response.getMessage()),
                () -> assertEquals("", response.getPath())
        );
    }

    @Test
    @DisplayName("Should handle negative status code")
    void shouldHandleNegativeStatusCode() {
        // When
        ErrorResponse response = ErrorResponse.builder()
                .status(-1)
                .build();

        // Then
        assertEquals(-1, response.getStatus());
    }
}