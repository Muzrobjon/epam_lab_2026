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

@DisplayName("SetActiveRequest Tests")
class SetActiveRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SetActiveRequest createValidRequest() {
        return SetActiveRequest.builder()
                .username("John.Doe")
                .password("password123")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields and isActive true")
        void shouldPassValidationWithAllValidFieldsAndIsActiveTrue() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("John.Doe")
                    .password("password123")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with all valid fields and isActive false")
        void shouldPassValidationWithAllValidFieldsAndIsActiveFalse() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("John.Doe")
                    .password("password123")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("a")
                    .password("b")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("a".repeat(255))
                    .password("b".repeat(255))
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when username is blank or null")
        void shouldFailValidationWhenUsernameIsBlankOrNull(String username) {
            SetActiveRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            SetActiveRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            SetActiveRequest request = createValidRequest();
            request.setUsername("john.doe@test_123-user");

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when password is blank or null")
        void shouldFailValidationWhenPasswordIsBlankOrNull(String password) {
            SetActiveRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            SetActiveRequest request = createValidRequest();
            request.setPassword("validPassword");

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            SetActiveRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%&*()");

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsActive Validation Tests")
    class IsActiveValidationTests {

        @Test
        @DisplayName("Should fail validation when isActive is null")
        void shouldFailValidationWhenIsActiveIsNull() {
            SetActiveRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("IsActive status is required");
        }

        @Test
        @DisplayName("Should pass validation when isActive is true")
        void shouldPassValidationWhenIsActiveIsTrue() {
            SetActiveRequest request = createValidRequest();
            request.setIsActive(true);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when isActive is false")
        void shouldPassValidationWhenIsActiveIsFalse() {
            SetActiveRequest request = createValidRequest();
            request.setIsActive(false);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username(null)
                    .password(null)
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Username is required",
                            "Password is required",
                            "IsActive status is required"
                    );
        }

        @Test
        @DisplayName("Should return violations for empty strings and null boolean")
        void shouldReturnViolationsForEmptyStringsAndNullBoolean() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("")
                    .password("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return violations for blank strings and null boolean")
        void shouldReturnViolationsForBlankStringsAndNullBoolean() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("   ")
                    .password("   ")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("valid.user")
                    .password(null)
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Password is required",
                            "IsActive status is required"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("test.user")
                    .password("testPass")
                    .isActive(true)
                    .build();

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            SetActiveRequest request = new SetActiveRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getPassword()).isNull();
            assertThat(request.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            SetActiveRequest request = new SetActiveRequest("test.user", "testPass", false);

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
            assertThat(request.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            SetActiveRequest request = new SetActiveRequest();

            request.setUsername("test.user");
            request.setPassword("testPass");
            request.setIsActive(true);

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            SetActiveRequest request1 = createValidRequest();
            SetActiveRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different isActive values")
        void shouldHaveCorrectEqualsForDifferentIsActiveValues() {
            SetActiveRequest request1 = createValidRequest();
            request1.setIsActive(true);

            SetActiveRequest request2 = createValidRequest();
            request2.setIsActive(false);

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct equals for different username")
        void shouldHaveCorrectEqualsForDifferentUsername() {
            SetActiveRequest request1 = createValidRequest();
            SetActiveRequest request2 = createValidRequest();
            request2.setUsername("different.user");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            SetActiveRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            SetActiveRequest request = createValidRequest();

            assertThat(request).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            SetActiveRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            SetActiveRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("SetActiveRequest");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("password=password123");
            assertThat(toString).contains("isActive=true");
        }

        @Test
        @DisplayName("Should have correct toString with isActive false")
        void shouldHaveCorrectToStringWithIsActiveFalse() {
            SetActiveRequest request = createValidRequest();
            request.setIsActive(false);

            String toString = request.toString();

            assertThat(toString).contains("isActive=false");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("josé.müller")
                    .password("пароль密码")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji")
        void shouldPassValidationWithEmoji() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("user😀")
                    .password("pass🔒word")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with numeric username")
        void shouldPassValidationWithNumericUsername() {
            SetActiveRequest request = SetActiveRequest.builder()
                    .username("123456789")
                    .password("password")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle Boolean wrapper correctly")
        void shouldHandleBooleanWrapperCorrectly() {
            SetActiveRequest request = new SetActiveRequest();
            request.setUsername("user");
            request.setPassword("pass");
            request.setIsActive(Boolean.TRUE);

            assertThat(request.getIsActive()).isTrue();
            assertThat(request.getIsActive()).isEqualTo(Boolean.TRUE);

            request.setIsActive(Boolean.FALSE);
            assertThat(request.getIsActive()).isFalse();
            assertThat(request.getIsActive()).isEqualTo(Boolean.FALSE);
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            SetActiveRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            SetActiveRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }

        @Test
        @DisplayName("Should have correct property path for isActive violation")
        void shouldHaveCorrectPropertyPathForIsActiveViolation() {
            SetActiveRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<SetActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("isActive");
        }
    }

    @Nested
    @DisplayName("Idempotent Behavior Tests")
    class IdempotentBehaviorTests {

        @Test
        @DisplayName("Should allow setting same active status multiple times")
        void shouldAllowSettingSameActiveStatusMultipleTimes() {
            SetActiveRequest request1 = SetActiveRequest.builder()
                    .username("user")
                    .password("pass")
                    .isActive(true)
                    .build();

            SetActiveRequest request2 = SetActiveRequest.builder()
                    .username("user")
                    .password("pass")
                    .isActive(true)
                    .build();

            // Both requests are valid and equal - supports idempotent behavior
            assertThat(validator.validate(request1)).isEmpty();
            assertThat(validator.validate(request2)).isEmpty();
            assertThat(request1).isEqualTo(request2);
        }

        @Test
        @DisplayName("Should distinguish between activate and deactivate requests")
        void shouldDistinguishBetweenActivateAndDeactivateRequests() {
            SetActiveRequest activateRequest = SetActiveRequest.builder()
                    .username("user")
                    .password("pass")
                    .isActive(true)
                    .build();

            SetActiveRequest deactivateRequest = SetActiveRequest.builder()
                    .username("user")
                    .password("pass")
                    .isActive(false)
                    .build();

            assertThat(activateRequest).isNotEqualTo(deactivateRequest);
            assertThat(activateRequest.getIsActive()).isNotEqualTo(deactivateRequest.getIsActive());
        }
    }
}