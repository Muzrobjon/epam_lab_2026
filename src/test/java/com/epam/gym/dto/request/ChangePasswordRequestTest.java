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

@DisplayName("ChangePasswordRequest Tests")
class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ChangePasswordRequest createValidRequest() {
        return ChangePasswordRequest.builder()
                .username("John.Doe")
                .oldPassword("oldPassword123")
                .newPassword("newPassword123")
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            ChangePasswordRequest request = createValidRequest();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("a")
                    .oldPassword("b")
                    .newPassword("c")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("a".repeat(100))
                    .oldPassword("b".repeat(100))
                    .newPassword("c".repeat(100))
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

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
            ChangePasswordRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            ChangePasswordRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            ChangePasswordRequest request = createValidRequest();
            request.setUsername("john.doe@test_123");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Old Password Validation Tests")
    class OldPasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when old password is blank or null")
        void shouldFailValidationWhenOldPasswordIsBlankOrNull(String oldPassword) {
            ChangePasswordRequest request = createValidRequest();
            request.setOldPassword(oldPassword);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Old password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid old password")
        void shouldPassValidationWithValidOldPassword() {
            ChangePasswordRequest request = createValidRequest();
            request.setOldPassword("validOldPassword");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with old password containing special characters")
        void shouldPassValidationWithOldPasswordContainingSpecialCharacters() {
            ChangePasswordRequest request = createValidRequest();
            request.setOldPassword("P@$$w0rd!#%&*");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("New Password Validation Tests")
    class NewPasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when new password is blank or null")
        void shouldFailValidationWhenNewPasswordIsBlankOrNull(String newPassword) {
            ChangePasswordRequest request = createValidRequest();
            request.setNewPassword(newPassword);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("New password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid new password")
        void shouldPassValidationWithValidNewPassword() {
            ChangePasswordRequest request = createValidRequest();
            request.setNewPassword("validNewPassword");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with new password containing special characters")
        void shouldPassValidationWithNewPasswordContainingSpecialCharacters() {
            ChangePasswordRequest request = createValidRequest();
            request.setNewPassword("N3w_P@$$w0rd!");

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username(null)
                    .oldPassword(null)
                    .newPassword(null)
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Username is required",
                            "Old password is required",
                            "New password is required"
                    );
        }

        @Test
        @DisplayName("Should return all violations when all fields are empty")
        void shouldReturnAllViolationsWhenAllFieldsAreEmpty() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("")
                    .oldPassword("")
                    .newPassword("")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return all violations when all fields are blank")
        void shouldReturnAllViolationsWhenAllFieldsAreBlank() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("   ")
                    .oldPassword("   ")
                    .newPassword("   ")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("valid.user")
                    .oldPassword(null)
                    .newPassword("")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Old password is required",
                            "New password is required"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("test.user")
                    .oldPassword("oldPass")
                    .newPassword("newPass")
                    .build();

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getOldPassword()).isEqualTo("oldPass");
            assertThat(request.getNewPassword()).isEqualTo("newPass");
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            ChangePasswordRequest request = new ChangePasswordRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getOldPassword()).isNull();
            assertThat(request.getNewPassword()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "test.user",
                    "oldPass",
                    "newPass"
            );

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getOldPassword()).isEqualTo("oldPass");
            assertThat(request.getNewPassword()).isEqualTo("newPass");
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            ChangePasswordRequest request = new ChangePasswordRequest();

            request.setUsername("test.user");
            request.setOldPassword("oldPass");
            request.setNewPassword("newPass");

            assertThat(request.getUsername()).isEqualTo("test.user");
            assertThat(request.getOldPassword()).isEqualTo("oldPass");
            assertThat(request.getNewPassword()).isEqualTo("newPass");
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            ChangePasswordRequest request1 = createValidRequest();
            ChangePasswordRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals implementation for different objects")
        void shouldHaveCorrectEqualsImplementationForDifferentObjects() {
            ChangePasswordRequest request1 = createValidRequest();
            ChangePasswordRequest request2 = createValidRequest();
            request2.setNewPassword("differentPassword");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            ChangePasswordRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            ChangePasswordRequest request = createValidRequest();

            assertThat(request).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            ChangePasswordRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            ChangePasswordRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("ChangePasswordRequest");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("oldPassword=oldPassword123");
            assertThat(toString).contains("newPassword=newPassword123");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with same old and new password")
        void shouldPassValidationWithSameOldAndNewPassword() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("test.user")
                    .oldPassword("samePassword")
                    .newPassword("samePassword")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            // Validation passes - business logic should handle same password check
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("josé.müller")
                    .oldPassword("пароль123")
                    .newPassword("密码456")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with numeric username")
        void shouldPassValidationWithNumericUsername() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("123456")
                    .oldPassword("oldPass")
                    .newPassword("newPass")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing whitespace in middle")
        void shouldPassValidationWithPasswordContainingWhitespaceInMiddle() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("test.user")
                    .oldPassword("old password")
                    .newPassword("new password")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji in fields")
        void shouldPassValidationWithEmojiInFields() {
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .username("user😀")
                    .oldPassword("pass🔒")
                    .newPassword("new🔑pass")
                    .build();

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Field Property Path Tests")
    class FieldPropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            ChangePasswordRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for oldPassword violation")
        void shouldHaveCorrectPropertyPathForOldPasswordViolation() {
            ChangePasswordRequest request = createValidRequest();
            request.setOldPassword(null);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("oldPassword");
        }

        @Test
        @DisplayName("Should have correct property path for newPassword violation")
        void shouldHaveCorrectPropertyPathForNewPasswordViolation() {
            ChangePasswordRequest request = createValidRequest();
            request.setNewPassword(null);

            Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("newPassword");
        }
    }
}