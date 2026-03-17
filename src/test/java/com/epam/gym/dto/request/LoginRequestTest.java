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

@DisplayName("LoginRequest Tests")
class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private LoginRequest createValidRequest() {
        return LoginRequest.builder()
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
            LoginRequest request = createValidRequest();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            LoginRequest request = LoginRequest.builder()
                    .username("a")
                    .password("b")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            LoginRequest request = LoginRequest.builder()
                    .username("a".repeat(255))
                    .password("b".repeat(255))
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

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
            LoginRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            LoginRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            LoginRequest request = createValidRequest();
            request.setUsername("john.doe@test_123-user");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with numeric username")
        void shouldPassValidationWithNumericUsername() {
            LoginRequest request = createValidRequest();
            request.setUsername("123456789");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

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
            LoginRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            LoginRequest request = createValidRequest();
            request.setPassword("validPassword");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            LoginRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%&*()");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing whitespace in middle")
        void shouldPassValidationWithPasswordContainingWhitespaceInMiddle() {
            LoginRequest request = createValidRequest();
            request.setPassword("pass word 123");

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            LoginRequest request = LoginRequest.builder()
                    .username(null)
                    .password(null)
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

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
            LoginRequest request = LoginRequest.builder()
                    .username("")
                    .password("")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }

        @Test
        @DisplayName("Should return all violations when all fields are blank")
        void shouldReturnAllViolationsWhenAllFieldsAreBlank() {
            LoginRequest request = LoginRequest.builder()
                    .username("   ")
                    .password("   ")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            LoginRequest request = LoginRequest.builder()
                    .username("test.user")
                    .password("testPass")
                    .build();

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            LoginRequest request = new LoginRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            LoginRequest request = new LoginRequest("test.user", "testPass");

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            LoginRequest request = new LoginRequest();

            request.setUsername("test.user");
            request.setPassword("testPass");

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getPassword()).isEqualTo("testPass");
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            LoginRequest request1 = createValidRequest();
            LoginRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different objects")
        void shouldHaveCorrectEqualsForDifferentObjects() {
            LoginRequest request1 = createValidRequest();
            LoginRequest request2 = createValidRequest();
            request2.setPassword("differentPassword");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            LoginRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            LoginRequest request = createValidRequest();

            assertThat(request).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            LoginRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            LoginRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("LoginRequest");
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
            LoginRequest request = LoginRequest.builder()
                    .username("josé.müller")
                    .password("пароль密码")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji")
        void shouldPassValidationWithEmoji() {
            LoginRequest request = LoginRequest.builder()
                    .username("user😀")
                    .password("pass🔒word")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with mixed case")
        void shouldPassValidationWithMixedCase() {
            LoginRequest request = LoginRequest.builder()
                    .username("JoHn.DoE")
                    .password("PaSsWoRd123")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with leading/trailing spaces in content")
        void shouldPassValidationWithContentHavingInternalSpaces() {
            LoginRequest request = LoginRequest.builder()
                    .username("john doe")
                    .password("pass word")
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            LoginRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            LoginRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }
    }
}