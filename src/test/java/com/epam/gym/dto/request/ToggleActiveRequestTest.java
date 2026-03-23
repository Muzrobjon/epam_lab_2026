package com.epam.gym.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ToggleActiveRequest Tests")
class ToggleActiveRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== VALID REQUEST TESTS ====================

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation when activating user")
        void shouldPassValidation_WhenActivatingUser() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when deactivating user")
        void shouldPassValidation_WhenDeactivatingUser() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "john.doe", "user123", "trainer.smith.1"})
        @DisplayName("Should pass validation with various valid usernames")
        void shouldPassValidation_WithVariousValidUsernames(String username) {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(username)
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== USERNAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should fail validation when username is blank or null")
        void shouldFailValidation_WhenUsernameIsBlankOrNull(String username) {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(username)
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        }

        @Test
        @DisplayName("Should have correct error message for null username")
        void shouldHaveCorrectErrorMessage_WhenUsernameIsNull() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(null)
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should have correct error message for empty username")
        void shouldHaveCorrectErrorMessage_WhenUsernameIsEmpty() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should have correct error message for blank username")
        void shouldHaveCorrectErrorMessage_WhenUsernameIsBlank() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("   ")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }
    }

    // ==================== IS_ACTIVE VALIDATION TESTS ====================

    @Nested
    @DisplayName("IsActive Validation Tests")
    class IsActiveValidationTests {

        @Test
        @DisplayName("Should fail validation when isActive is null")
        void shouldFailValidation_WhenIsActiveIsNull() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("isActive"));
        }

        @Test
        @DisplayName("Should have correct error message for null isActive")
        void shouldHaveCorrectErrorMessage_WhenIsActiveIsNull() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("IsActive is required");
        }

        @Test
        @DisplayName("Should pass validation when isActive is true")
        void shouldPassValidation_WhenIsActiveIsTrue() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(Boolean.TRUE)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should pass validation when isActive is false")
        void shouldPassValidation_WhenIsActiveIsFalse() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(Boolean.FALSE)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
            assertThat(request.getIsActive()).isFalse();
        }
    }

    // ==================== MULTIPLE VALIDATION ERRORS TESTS ====================

    @Nested
    @DisplayName("Multiple Validation Errors Tests")
    class MultipleValidationErrorsTests {

        @Test
        @DisplayName("Should return all violations when both fields are invalid")
        void shouldReturnAllViolations_WhenBothFieldsAreInvalid() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("username", "isActive");
        }

        @Test
        @DisplayName("Should return all violations when both fields are null")
        void shouldReturnAllViolations_WhenBothFieldsAreNull() {
            ToggleActiveRequest request = new ToggleActiveRequest();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder("Username is required", "IsActive is required");
        }

        @Test
        @DisplayName("Should return all violations with blank username and null isActive")
        void shouldReturnAllViolations_WhenUsernameIsBlankAndIsActiveIsNull() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("   ")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }
    }

    // ==================== LOMBOK FUNCTIONALITY TESTS ====================

    @Nested
    @DisplayName("Lombok Functionality Tests")
    class LombokFunctionalityTests {

        @Test
        @DisplayName("Should create object using builder")
        void shouldCreateObject_UsingBuilder() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            assertThat(request.getUsername()).isEqualTo("john.doe");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create object using no-args constructor and setters")
        void shouldCreateObject_UsingNoArgsConstructorAndSetters() {
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setUsername("john.doe");
            request.setIsActive(false);

            assertThat(request.getUsername()).isEqualTo("john.doe");
            assertThat(request.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should create object using all-args constructor")
        void shouldCreateObject_UsingAllArgsConstructor() {
            ToggleActiveRequest request = new ToggleActiveRequest("john.doe", true);

            assertThat(request.getUsername()).isEqualTo("john.doe");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should have correct equals for identical objects")
        void shouldHaveCorrectEquals_ForIdenticalObjects() {
            ToggleActiveRequest request1 = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            ToggleActiveRequest request2 = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different usernames")
        void shouldHaveCorrectEquals_ForDifferentUsernames() {
            ToggleActiveRequest request1 = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            ToggleActiveRequest request2 = ToggleActiveRequest.builder()
                    .username("jane.doe")
                    .isActive(true)
                    .build();

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct equals for different isActive values")
        void shouldHaveCorrectEquals_ForDifferentIsActiveValues() {
            ToggleActiveRequest request1 = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            ToggleActiveRequest request2 = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(false)
                    .build();

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct toString with true value")
        void shouldHaveCorrectToString_WithTrueValue() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(true)
                    .build();

            String toString = request.toString();

            assertThat(toString).contains("john.doe");
            assertThat(toString).contains("true");
        }

        @Test
        @DisplayName("Should have correct toString with false value")
        void shouldHaveCorrectToString_WithFalseValue() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe")
                    .isActive(false)
                    .build();

            String toString = request.toString();

            assertThat(toString).contains("john.doe");
            assertThat(toString).contains("false");
        }
    }

    // ==================== EDGE CASES TESTS ====================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle username with special characters")
        void shouldHandle_UsernameWithSpecialCharacters() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john.doe_123")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandle_VeryLongUsername() {
            String longUsername = "a".repeat(255);

            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(longUsername)
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle single character username")
        void shouldHandle_SingleCharacterUsername() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("a")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandle_UsernameWithUnicodeCharacters() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("用户名")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}