package com.epam.gym.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    private ErrorResponse createValidResponse() {
        return ErrorResponse.builder()
                .transactionId("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
                .message("User not found")
                .status(404)
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .path("/api/trainees/John.Doe")
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-123")
                    .message("Resource not found")
                    .status(404)
                    .timestamp(timestamp)
                    .path("/api/users/1")
                    .build();

            assertThat(response.getTransactionId()).isEqualTo("txn-123");
            assertThat(response.getMessage()).isEqualTo("Resource not found");
            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getTimestamp()).isEqualTo(timestamp);
            assertThat(response.getPath()).isEqualTo("/api/users/1");
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId(null)
                    .message(null)
                    .status(0)
                    .timestamp(null)
                    .path(null)
                    .build();

            assertThat(response.getTransactionId()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getStatus()).isZero();
            assertThat(response.getTimestamp()).isNull();
            assertThat(response.getPath()).isNull();
        }

        @Test
        @DisplayName("Should create response with partial fields using builder")
        void shouldCreateResponseWithPartialFieldsUsingBuilder() {
            ErrorResponse response = ErrorResponse.builder()
                    .message("Error occurred")
                    .status(500)
                    .build();

            assertThat(response.getTransactionId()).isNull();
            assertThat(response.getMessage()).isEqualTo("Error occurred");
            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getTimestamp()).isNull();
            assertThat(response.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            ErrorResponse response = new ErrorResponse();

            assertThat(response.getTransactionId()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getStatus()).isZero();
            assertThat(response.getTimestamp()).isNull();
            assertThat(response.getPath()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            LocalDateTime timestamp = LocalDateTime.now();

            ErrorResponse response = new ErrorResponse(
                    "txn-456",
                    "Bad request",
                    400,
                    timestamp,
                    "/api/trainers"
            );

            assertThat(response.getTransactionId()).isEqualTo("txn-456");
            assertThat(response.getMessage()).isEqualTo("Bad request");
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getTimestamp()).isEqualTo(timestamp);
            assertThat(response.getPath()).isEqualTo("/api/trainers");
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get transactionId")
        void shouldSetAndGetTransactionId() {
            ErrorResponse response = new ErrorResponse();

            response.setTransactionId("new-txn-id");

            assertThat(response.getTransactionId()).isEqualTo("new-txn-id");
        }

        @Test
        @DisplayName("Should set and get message")
        void shouldSetAndGetMessage() {
            ErrorResponse response = new ErrorResponse();

            response.setMessage("New error message");

            assertThat(response.getMessage()).isEqualTo("New error message");
        }

        @Test
        @DisplayName("Should set and get status")
        void shouldSetAndGetStatus() {
            ErrorResponse response = new ErrorResponse();

            response.setStatus(503);

            assertThat(response.getStatus()).isEqualTo(503);
        }

        @Test
        @DisplayName("Should set and get timestamp")
        void shouldSetAndGetTimestamp() {
            ErrorResponse response = new ErrorResponse();
            LocalDateTime timestamp = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

            response.setTimestamp(timestamp);

            assertThat(response.getTimestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should set and get path")
        void shouldSetAndGetPath() {
            ErrorResponse response = new ErrorResponse();

            response.setPath("/api/new/path");

            assertThat(response.getPath()).isEqualTo("/api/new/path");
        }

        @Test
        @DisplayName("Should allow setting all fields to null")
        void shouldAllowSettingAllFieldsToNull() {
            ErrorResponse response = createValidResponse();

            response.setTransactionId(null);
            response.setMessage(null);
            response.setTimestamp(null);
            response.setPath(null);

            assertThat(response.getTransactionId()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getTimestamp()).isNull();
            assertThat(response.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @ParameterizedTest
        @ValueSource(ints = {200, 201, 204})
        @DisplayName("Should accept 2xx status codes")
        void shouldAccept2xxStatusCodes(int status) {
            ErrorResponse response = ErrorResponse.builder()
                    .status(status)
                    .build();

            assertThat(response.getStatus()).isEqualTo(status);
        }

        @ParameterizedTest
        @ValueSource(ints = {400, 401, 403, 404, 405, 409, 422})
        @DisplayName("Should accept 4xx status codes")
        void shouldAccept4xxStatusCodes(int status) {
            ErrorResponse response = ErrorResponse.builder()
                    .status(status)
                    .build();

            assertThat(response.getStatus()).isEqualTo(status);
        }

        @ParameterizedTest
        @ValueSource(ints = {500, 501, 502, 503, 504})
        @DisplayName("Should accept 5xx status codes")
        void shouldAccept5xxStatusCodes(int status) {
            ErrorResponse response = ErrorResponse.builder()
                    .status(status)
                    .build();

            assertThat(response.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should accept zero status code")
        void shouldAcceptZeroStatusCode() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(0)
                    .build();

            assertThat(response.getStatus()).isZero();
        }

        @Test
        @DisplayName("Should accept negative status code")
        void shouldAcceptNegativeStatusCode() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(-1)
                    .build();

            assertThat(response.getStatus()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should handle current timestamp")
        void shouldHandleCurrentTimestamp() {
            LocalDateTime now = LocalDateTime.now();

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(now)
                    .build();

            assertThat(response.getTimestamp()).isEqualTo(now);
        }

        @Test
        @DisplayName("Should handle past timestamp")
        void shouldHandlePastTimestamp() {
            LocalDateTime past = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(past)
                    .build();

            assertThat(response.getTimestamp()).isEqualTo(past);
        }

        @Test
        @DisplayName("Should handle future timestamp")
        void shouldHandleFutureTimestamp() {
            LocalDateTime future = LocalDateTime.of(2030, 12, 31, 23, 59, 59);

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(future)
                    .build();

            assertThat(response.getTimestamp()).isEqualTo(future);
        }

        @Test
        @DisplayName("Should handle timestamp with nanoseconds")
        void shouldHandleTimestampWithNanoseconds() {
            LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123456789);

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(timestamp)
                    .build();

            assertThat(response.getTimestamp()).isEqualTo(timestamp);
            assertThat(response.getTimestamp().getNano()).isEqualTo(123456789);
        }
    }

    @Nested
    @DisplayName("Path Tests")
    class PathTests {

        @Test
        @DisplayName("Should handle simple path")
        void shouldHandleSimplePath() {
            ErrorResponse response = ErrorResponse.builder()
                    .path("/api/users")
                    .build();

            assertThat(response.getPath()).isEqualTo("/api/users");
        }

        @Test
        @DisplayName("Should handle path with path variables")
        void shouldHandlePathWithPathVariables() {
            ErrorResponse response = ErrorResponse.builder()
                    .path("/api/users/123/trainings/456")
                    .build();

            assertThat(response.getPath()).isEqualTo("/api/users/123/trainings/456");
        }

        @Test
        @DisplayName("Should handle path with query parameters")
        void shouldHandlePathWithQueryParameters() {
            ErrorResponse response = ErrorResponse.builder()
                    .path("/api/trainings?from=2024-01-01&to=2024-12-31")
                    .build();

            assertThat(response.getPath()).isEqualTo("/api/trainings?from=2024-01-01&to=2024-12-31");
        }

        @Test
        @DisplayName("Should handle empty path")
        void shouldHandleEmptyPath() {
            ErrorResponse response = ErrorResponse.builder()
                    .path("")
                    .build();

            assertThat(response.getPath()).isEmpty();
        }

        @Test
        @DisplayName("Should handle root path")
        void shouldHandleRootPath() {
            ErrorResponse response = ErrorResponse.builder()
                    .path("/")
                    .build();

            assertThat(response.getPath()).isEqualTo("/");
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("Should handle simple error message")
        void shouldHandleSimpleErrorMessage() {
            ErrorResponse response = ErrorResponse.builder()
                    .message("User not found")
                    .build();

            assertThat(response.getMessage()).isEqualTo("User not found");
        }

        @Test
        @DisplayName("Should handle detailed error message")
        void shouldHandleDetailedErrorMessage() {
            String detailedMessage = "Validation failed for field 'email': must be a valid email address";

            ErrorResponse response = ErrorResponse.builder()
                    .message(detailedMessage)
                    .build();

            assertThat(response.getMessage()).isEqualTo(detailedMessage);
        }

        @Test
        @DisplayName("Should handle multiline error message")
        void shouldHandleMultilineErrorMessage() {
            String multilineMessage = "Multiple errors occurred:\n- Field 'name' is required\n- Field 'email' is invalid";

            ErrorResponse response = ErrorResponse.builder()
                    .message(multilineMessage)
                    .build();

            assertThat(response.getMessage()).isEqualTo(multilineMessage);
        }

        @Test
        @DisplayName("Should handle empty error message")
        void shouldHandleEmptyErrorMessage() {
            ErrorResponse response = ErrorResponse.builder()
                    .message("")
                    .build();

            assertThat(response.getMessage()).isEmpty();
        }

        @Test
        @DisplayName("Should handle error message with special characters")
        void shouldHandleErrorMessageWithSpecialCharacters() {
            String message = "Error: <invalid> & 'special' \"characters\" © ®";

            ErrorResponse response = ErrorResponse.builder()
                    .message(message)
                    .build();

            assertThat(response.getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Transaction ID Tests")
    class TransactionIdTests {

        @Test
        @DisplayName("Should handle UUID format transaction ID")
        void shouldHandleUuidFormatTransactionId() {
            String uuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

            ErrorResponse response = ErrorResponse.builder()
                    .transactionId(uuid)
                    .build();

            assertThat(response.getTransactionId()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("Should handle custom format transaction ID")
        void shouldHandleCustomFormatTransactionId() {
            String customId = "TXN-2024-001-ABC";

            ErrorResponse response = ErrorResponse.builder()
                    .transactionId(customId)
                    .build();

            assertThat(response.getTransactionId()).isEqualTo(customId);
        }

        @Test
        @DisplayName("Should handle empty transaction ID")
        void shouldHandleEmptyTransactionId() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("")
                    .build();

            assertThat(response.getTransactionId()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            ErrorResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different transactionId")
        void shouldNotBeEqualToResponseWithDifferentTransactionId() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();
            response2.setTransactionId("different-id");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different message")
        void shouldNotBeEqualToResponseWithDifferentMessage() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();
            response2.setMessage("Different message");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different status")
        void shouldNotBeEqualToResponseWithDifferentStatus() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();
            response2.setStatus(500);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different timestamp")
        void shouldNotBeEqualToResponseWithDifferentTimestamp() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();
            response2.setTimestamp(LocalDateTime.now());

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different path")
        void shouldNotBeEqualToResponseWithDifferentPath() {
            ErrorResponse response1 = createValidResponse();
            ErrorResponse response2 = createValidResponse();
            response2.setPath("/different/path");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ErrorResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ErrorResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not an ErrorResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            ErrorResponse response1 = new ErrorResponse();
            ErrorResponse response2 = new ErrorResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            ErrorResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("ErrorResponse");
            assertThat(toString).contains("transactionId=a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            assertThat(toString).contains("message=User not found");
            assertThat(toString).contains("status=404");
            assertThat(toString).contains("path=/api/trainees/John.Doe");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            ErrorResponse response = new ErrorResponse();

            String toString = response.toString();

            assertThat(toString).contains("ErrorResponse");
            assertThat(toString).contains("transactionId=null");
            assertThat(toString).contains("message=null");
            assertThat(toString).contains("status=0");
            assertThat(toString).contains("timestamp=null");
            assertThat(toString).contains("path=null");
        }
    }

    @Nested
    @DisplayName("Common Error Scenarios Tests")
    class CommonErrorScenariosTests {

        @Test
        @DisplayName("Should create 400 Bad Request error response")
        void shouldCreate400BadRequestErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-001")
                    .message("Invalid request body")
                    .status(400)
                    .timestamp(LocalDateTime.now())
                    .path("/api/trainees")
                    .build();

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getMessage()).isEqualTo("Invalid request body");
        }

        @Test
        @DisplayName("Should create 401 Unauthorized error response")
        void shouldCreate401UnauthorizedErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-002")
                    .message("Authentication required")
                    .status(401)
                    .timestamp(LocalDateTime.now())
                    .path("/api/trainers")
                    .build();

            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getMessage()).isEqualTo("Authentication required");
        }

        @Test
        @DisplayName("Should create 403 Forbidden error response")
        void shouldCreate403ForbiddenErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-003")
                    .message("Access denied")
                    .status(403)
                    .timestamp(LocalDateTime.now())
                    .path("/api/admin/users")
                    .build();

            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getMessage()).isEqualTo("Access denied");
        }

        @Test
        @DisplayName("Should create 404 Not Found error response")
        void shouldCreate404NotFoundErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-004")
                    .message("Trainee not found")
                    .status(404)
                    .timestamp(LocalDateTime.now())
                    .path("/api/trainees/unknown.user")
                    .build();

            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(response.getMessage()).isEqualTo("Trainee not found");
        }

        @Test
        @DisplayName("Should create 409 Conflict error response")
        void shouldCreate409ConflictErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-005")
                    .message("Username already exists")
                    .status(409)
                    .timestamp(LocalDateTime.now())
                    .path("/api/trainees")
                    .build();

            assertThat(response.getStatus()).isEqualTo(409);
            assertThat(response.getMessage()).isEqualTo("Username already exists");
        }

        @Test
        @DisplayName("Should create 500 Internal Server Error response")
        void shouldCreate500InternalServerErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-006")
                    .message("An unexpected error occurred")
                    .status(500)
                    .timestamp(LocalDateTime.now())
                    .path("/api/trainings")
                    .build();

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
        }

        @Test
        @DisplayName("Should create 503 Service Unavailable error response")
        void shouldCreate503ServiceUnavailableErrorResponse() {
            ErrorResponse response = ErrorResponse.builder()
                    .transactionId("txn-007")
                    .message("Service temporarily unavailable")
                    .status(503)
                    .timestamp(LocalDateTime.now())
                    .path("/api/health")
                    .build();

            assertThat(response.getStatus()).isEqualTo(503);
            assertThat(response.getMessage()).isEqualTo("Service temporarily unavailable");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long message")
        void shouldHandleVeryLongMessage() {
            String longMessage = "Error: " + "x".repeat(10000);

            ErrorResponse response = ErrorResponse.builder()
                    .message(longMessage)
                    .build();

            assertThat(response.getMessage()).hasSize(10007);
        }

        @Test
        @DisplayName("Should handle very long path")
        void shouldHandleVeryLongPath() {
            String longPath = "/api/" + "segment/".repeat(100);

            ErrorResponse response = ErrorResponse.builder()
                    .path(longPath)
                    .build();

            assertThat(response.getPath()).startsWith("/api/");
        }

        @Test
        @DisplayName("Should handle unicode in message")
        void shouldHandleUnicodeInMessage() {
            String unicodeMessage = "Ошибка: пользователь не найден 用户未找到";

            ErrorResponse response = ErrorResponse.builder()
                    .message(unicodeMessage)
                    .build();

            assertThat(response.getMessage()).isEqualTo(unicodeMessage);
        }

        @Test
        @DisplayName("Should handle maximum int status")
        void shouldHandleMaximumIntStatus() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(Integer.MAX_VALUE)
                    .build();

            assertThat(response.getStatus()).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should handle minimum int status")
        void shouldHandleMinimumIntStatus() {
            ErrorResponse response = ErrorResponse.builder()
                    .status(Integer.MIN_VALUE)
                    .build();

            assertThat(response.getStatus()).isEqualTo(Integer.MIN_VALUE);
        }
    }
}