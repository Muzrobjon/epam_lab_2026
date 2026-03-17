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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateTraineeTrainersRequest Tests")
class UpdateTraineeTrainersRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UpdateTraineeTrainersRequest createValidRequest() {
        return UpdateTraineeTrainersRequest.builder()
                .traineeUsername("John.Doe")
                .password("password123")
                .trainerUsernames(Arrays.asList("Alice.Smith", "Bob.Johnson"))
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            UpdateTraineeTrainersRequest request = createValidRequest();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single trainer")
        void shouldPassValidationWithSingleTrainer() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("John.Doe")
                    .password("password123")
                    .trainerUsernames(Collections.singletonList("Alice.Smith"))
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with multiple trainers")
        void shouldPassValidationWithMultipleTrainers() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("John.Doe")
                    .password("password123")
                    .trainerUsernames(Arrays.asList("Trainer1", "Trainer2", "Trainer3", "Trainer4", "Trainer5"))
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("a")
                    .password("b")
                    .trainerUsernames(Collections.singletonList("c"))
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("a".repeat(100))
                    .password("b".repeat(100))
                    .trainerUsernames(Collections.singletonList("c".repeat(100)))
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainee Username Validation Tests")
    class TraineeUsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when trainee username is blank or null")
        void shouldFailValidationWhenTraineeUsernameIsBlankOrNull(String traineeUsername) {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTraineeUsername(traineeUsername);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainee username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid trainee username")
        void shouldPassValidationWithValidTraineeUsername() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTraineeUsername("valid.username");

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with trainee username containing special characters")
        void shouldPassValidationWithTraineeUsernameContainingSpecialCharacters() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTraineeUsername("john.doe@test_123");

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when password is blank or null")
        void shouldFailValidationWhenPasswordIsBlankOrNull(String password) {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setPassword("validPassword123");

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%");

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainer Usernames List Validation Tests")
    class TrainerUsernamesValidationTests {

        @Test
        @DisplayName("Should fail validation when trainer usernames is null")
        void shouldFailValidationWhenTrainerUsernamesIsNull() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(null);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainers list cannot be empty");
        }

        @Test
        @DisplayName("Should fail validation when trainer usernames is empty list")
        void shouldFailValidationWhenTrainerUsernamesIsEmptyList() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Collections.emptyList());

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainers list cannot be empty");
        }

        @Test
        @DisplayName("Should fail validation when trainer usernames is empty ArrayList")
        void shouldFailValidationWhenTrainerUsernamesIsEmptyArrayList() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(new ArrayList<>());

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainers list cannot be empty");
        }

        @Test
        @DisplayName("Should pass validation with single trainer username")
        void shouldPassValidationWithSingleTrainerUsername() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Collections.singletonList("Trainer.One"));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with multiple trainer usernames")
        void shouldPassValidationWithMultipleTrainerUsernames() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Arrays.asList("Trainer.One", "Trainer.Two", "Trainer.Three"));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with list containing null element")
        void shouldPassValidationWithListContainingNullElement() {
            // Note: @NotEmpty only checks if list is empty, not element validity
            UpdateTraineeTrainersRequest request = createValidRequest();
            List<String> trainers = new ArrayList<>();
            trainers.add(null);
            request.setTrainerUsernames(trainers);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            // @NotEmpty passes because list is not empty (has 1 element, even if null)
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with list containing empty string")
        void shouldPassValidationWithListContainingEmptyString() {
            // Note: @NotEmpty only checks if list is empty, not element validity
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Collections.singletonList(""));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            // @NotEmpty passes because list is not empty
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with large list of trainers")
        void shouldPassValidationWithLargeListOfTrainers() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            List<String> trainers = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                trainers.add("Trainer" + i);
            }
            request.setTrainerUsernames(trainers);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with duplicate trainer usernames")
        void shouldPassValidationWithDuplicateTrainerUsernames() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Arrays.asList("Same.Trainer", "Same.Trainer", "Same.Trainer"));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            // Validation doesn't check for duplicates
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername(null)
                    .password(null)
                    .trainerUsernames(null)
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Trainee username is required",
                            "Password is required",
                            "Trainers list cannot be empty"
                    );
        }

        @Test
        @DisplayName("Should return all violations when string fields are empty and list is empty")
        void shouldReturnAllViolationsWhenStringFieldsAreEmptyAndListIsEmpty() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("")
                    .password("")
                    .trainerUsernames(Collections.emptyList())
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return all violations when string fields are blank and list is empty")
        void shouldReturnAllViolationsWhenStringFieldsAreBlankAndListIsEmpty() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("   ")
                    .password("   ")
                    .trainerUsernames(new ArrayList<>())
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("valid.user")
                    .password(null)
                    .trainerUsernames(Collections.emptyList())
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Password is required",
                            "Trainers list cannot be empty"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            List<String> trainers = Arrays.asList("Alice.Smith", "Bob.Johnson");
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("john.doe")
                    .password("pass123")
                    .trainerUsernames(trainers)
                    .build();

            assertThat(request.getTraineeUsername()).isEqualTo("john.doe");
            assertThat(request.getPassword()).isEqualTo("pass123");
            assertThat(request.getTrainerUsernames()).isEqualTo(trainers);
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();

            assertThat(request.getTraineeUsername()).isNull();
            assertThat(request.getPassword()).isNull();
            assertThat(request.getTrainerUsernames()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            List<String> trainers = Arrays.asList("Trainer1", "Trainer2");
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                    "bob.wilson",
                    "pass456",
                    trainers
            );

            assertThat(request.getTraineeUsername()).isEqualTo("bob.wilson");
            assertThat(request.getPassword()).isEqualTo("pass456");
            assertThat(request.getTrainerUsernames()).isEqualTo(trainers);
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
            List<String> trainers = Arrays.asList("Trainer.A", "Trainer.B");

            request.setTraineeUsername("charlie.brown");
            request.setPassword("pass789");
            request.setTrainerUsernames(trainers);

            assertThat(request.getTraineeUsername()).isEqualTo("charlie.brown");
            assertThat(request.getPassword()).isEqualTo("pass789");
            assertThat(request.getTrainerUsernames()).isEqualTo(trainers);
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            UpdateTraineeTrainersRequest request1 = createValidRequest();
            UpdateTraineeTrainersRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different trainer lists")
        void shouldHaveCorrectEqualsForDifferentTrainerLists() {
            UpdateTraineeTrainersRequest request1 = createValidRequest();
            UpdateTraineeTrainersRequest request2 = createValidRequest();
            request2.setTrainerUsernames(Collections.singletonList("Different.Trainer"));

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct equals for different usernames")
        void shouldHaveCorrectEqualsForDifferentUsernames() {
            UpdateTraineeTrainersRequest request1 = createValidRequest();
            UpdateTraineeTrainersRequest request2 = createValidRequest();
            request2.setTraineeUsername("different.user");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            UpdateTraineeTrainersRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            UpdateTraineeTrainersRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            UpdateTraineeTrainersRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("UpdateTraineeTrainersRequest");
            assertThat(toString).contains("traineeUsername=John.Doe");
            assertThat(toString).contains("password=password123");
            assertThat(toString).contains("trainerUsernames=");
            assertThat(toString).contains("Alice.Smith");
            assertThat(toString).contains("Bob.Johnson");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("josé.müller")
                    .password("пароль123")
                    .trainerUsernames(Arrays.asList("Тренер.Один", "教练.二"))
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with special characters in trainer names")
        void shouldPassValidationWithSpecialCharactersInTrainerNames() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Arrays.asList(
                    "trainer@domain.com",
                    "trainer_name",
                    "trainer-name",
                    "trainer.name.123"
            ));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle mutable list correctly")
        void shouldHandleMutableListCorrectly() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            List<String> mutableList = new ArrayList<>(Arrays.asList("Trainer1", "Trainer2"));
            request.setTrainerUsernames(mutableList);

            // Modify the list after setting
            mutableList.add("Trainer3");

            // The request's list should also be modified (no defensive copy)
            assertThat(request.getTrainerUsernames()).hasSize(3);
        }

        @Test
        @DisplayName("Should pass validation with whitespace in trainer names")
        void shouldPassValidationWithWhitespaceInTrainerNames() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Arrays.asList("Trainer With Spaces", "  Leading", "Trailing  "));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            // @NotEmpty doesn't validate individual elements
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with mixed valid and blank trainer names")
        void shouldPassValidationWithMixedValidAndBlankTrainerNames() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(Arrays.asList("Valid.Trainer", "", "   ", "Another.Valid"));

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            // @NotEmpty only checks list is not empty, not element content
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for traineeUsername violation")
        void shouldHaveCorrectPropertyPathForTraineeUsernameViolation() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTraineeUsername(null);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("traineeUsername");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }

        @Test
        @DisplayName("Should have correct property path for trainerUsernames violation")
        void shouldHaveCorrectPropertyPathForTrainerUsernamesViolation() {
            UpdateTraineeTrainersRequest request = createValidRequest();
            request.setTrainerUsernames(null);

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("trainerUsernames");
        }
    }

    @Nested
    @DisplayName("List Behavior Tests")
    class ListBehaviorTests {

        @Test
        @DisplayName("Should preserve order of trainer usernames")
        void shouldPreserveOrderOfTrainerUsernames() {
            List<String> orderedTrainers = Arrays.asList("First", "Second", "Third", "Fourth");
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("user")
                    .password("pass")
                    .trainerUsernames(orderedTrainers)
                    .build();

            assertThat(request.getTrainerUsernames())
                    .containsExactly("First", "Second", "Third", "Fourth");
        }

        @Test
        @DisplayName("Should handle immutable list")
        void shouldHandleImmutableList() {
            List<String> immutableList = List.of("Trainer1", "Trainer2");
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername("user")
                    .password("pass")
                    .trainerUsernames(immutableList)
                    .build();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
            assertThat(request.getTrainerUsernames()).hasSize(2);
        }

        @Test
        @DisplayName("Should correctly report list size")
        void shouldCorrectlyReportListSize() {
            UpdateTraineeTrainersRequest request = createValidRequest();

            assertThat(request.getTrainerUsernames()).hasSize(2);

            request.setTrainerUsernames(Collections.singletonList("Single"));
            assertThat(request.getTrainerUsernames()).hasSize(1);

            request.setTrainerUsernames(Arrays.asList("A", "B", "C", "D", "E"));
            assertThat(request.getTrainerUsernames()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("All Fields Required Tests")
    class AllFieldsRequiredTests {

        @Test
        @DisplayName("All fields should be required")
        void allFieldsShouldBeRequired() {
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();

            Set<ConstraintViolation<UpdateTraineeTrainersRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder(
                            "traineeUsername",
                            "password",
                            "trainerUsernames"
                    );
        }
    }
}