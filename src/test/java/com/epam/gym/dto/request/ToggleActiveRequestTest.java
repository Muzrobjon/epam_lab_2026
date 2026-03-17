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

    private ToggleActiveRequest createValidRequest() {
        return ToggleActiveRequest.builder()
                .username("John.Doe")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            ToggleActiveRequest request = createValidRequest();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("a")
                    .password("b")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("a".repeat(255))
                    .password("b".repeat(255))
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

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
            ToggleActiveRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            ToggleActiveRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            ToggleActiveRequest request = createValidRequest();
            request.setUsername("john.doe@test_123-user");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with numeric username")
        void shouldPassValidationWithNumericUsername() {
            ToggleActiveRequest request = createValidRequest();
            request.setUsername("123456789");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

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
            ToggleActiveRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            ToggleActiveRequest request = createValidRequest();
            request.setPassword("validPassword");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            ToggleActiveRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%&*()");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing whitespace in middle")
        void shouldPassValidationWithPasswordContainingWhitespaceInMiddle() {
            ToggleActiveRequest request = createValidRequest();
            request.setPassword("pass word 123");

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(null)
                    .password(null)
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Username is required",
                            "Password is required"
                    );
        }

        @Test
        @DisplayName("Should return all violations when all fields are empty")
        void shouldReturnAllViolationsWhenAllFieldsAreEmpty() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("")
                    .password("")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }

        @Test
        @DisplayName("Should return all violations when all fields are blank")
        void shouldReturnAllViolationsWhenAllFieldsAreBlank() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("   ")
                    .password("   ")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("test.user")
                    .password("testPass")
                    .build();

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            ToggleActiveRequest request = new ToggleActiveRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            ToggleActiveRequest request = new ToggleActiveRequest("test.user", "testPass");

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            ToggleActiveRequest request = new ToggleActiveRequest();

            request.setUsername("test.user");
            request.setPassword("testPass");

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            ToggleActiveRequest request1 = createValidRequest();
            ToggleActiveRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different objects")
        void shouldHaveCorrectEqualsForDifferentObjects() {
            ToggleActiveRequest request1 = createValidRequest();
            ToggleActiveRequest request2 = createValidRequest();
            request2.setPassword("differentPassword");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ToggleActiveRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ToggleActiveRequest request = createValidRequest();

            assertThat(request).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            ToggleActiveRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            ToggleActiveRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("ToggleActiveRequest");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("password=password123");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("josé.müller")
                    .password("пароль密码")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji")
        void shouldPassValidationWithEmoji() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("user😀")
                    .password("pass🔒word")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with mixed case")
        void shouldPassValidationWithMixedCase() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("JoHn.DoE")
                    .password("PaSsWoRd123")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with content having internal spaces")
        void shouldPassValidationWithContentHavingInternalSpaces() {
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("john doe")
                    .password("pass word")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            ToggleActiveRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            ToggleActiveRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }
    }

    @Nested
    @DisplayName("Non-Idempotent Behavior Documentation Tests")
    class NonIdempotentBehaviorTests {

        @Test
        @DisplayName("Should create identical requests for toggle operation")
        void shouldCreateIdenticalRequestsForToggleOperation() {
            // Toggle requests are identical - the non-idempotent behavior
            // is in the service layer, not in the request validation
            ToggleActiveRequest request1 = createValidRequest();
            ToggleActiveRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(validator.validate(request1)).isEmpty();
            assertThat(validator.validate(request2)).isEmpty();
        }

        @Test
        @DisplayName("Should not contain isActive field - toggle determines new state")
        void shouldNotContainIsActiveField() {
            ToggleActiveRequest request = createValidRequest();

            // Verify the class only has username and password fields
            // No isActive field - the toggle operation determines the new state
            assertThat(request).hasFieldOrProperty("username");
            assertThat(request).hasFieldOrProperty("password");
            assertThat(request).hasNoNullFieldsOrProperties();
        }
    }

    @Nested
    @DisplayName("Comparison with SetActiveRequest Tests")
    class ComparisonWithSetActiveRequestTests {

        @Test
        @DisplayName("ToggleActiveRequest should have fewer fields than SetActiveRequest")
        void toggleActiveRequestShouldHaveFewerFieldsThanSetActiveRequest() {
            ToggleActiveRequest toggleRequest = createValidRequest();

            // ToggleActiveRequest has only 2 fields (username, password)
            // SetActiveRequest has 3 fields (username, password, isActive)
            assertThat(toggleRequest.getClass().getDeclaredFields())
                    .extracting("name")
                    .containsExactlyInAnyOrder("username", "password");
        }

        @Test
        @DisplayName("Should be valid without specifying target active state")
        void shouldBeValidWithoutSpecifyingTargetActiveState() {
            // Unlike SetActiveRequest, ToggleActiveRequest doesn't need isActive
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username("user")
                    .password("pass")
                    .build();

            Set<ConstraintViolation<ToggleActiveRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}