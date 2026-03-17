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

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddTrainingRequest Tests")
class AddTrainingRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private AddTrainingRequest createValidRequest() {
        return AddTrainingRequest.builder()
                .traineeUsername("John.Doe")
                .traineePassword("traineePass123")
                .trainerUsername("Alice.Smith")
                .trainerPassword("trainerPass123")
                .trainingName("Morning Yoga Session")
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(60)
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            AddTrainingRequest request = createValidRequest();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum valid duration")
        void shouldPassValidationWithMinimumValidDuration() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(1);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with large duration")
        void shouldPassValidationWithLargeDuration() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(480); // 8 hours

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with future date")
        void shouldPassValidationWithFutureDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.now().plusMonths(1));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with past date")
        void shouldPassValidationWithPastDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.now().minusDays(1));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with today's date")
        void shouldPassValidationWithTodaysDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.now());

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainee Username Validation Tests")
    class TraineeUsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainee username is blank or null")
        void shouldFailValidationWhenTraineeUsernameIsBlankOrNull(String traineeUsername) {
            AddTrainingRequest request = createValidRequest();
            request.setTraineeUsername(traineeUsername);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainee username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid trainee username")
        void shouldPassValidationWithValidTraineeUsername() {
            AddTrainingRequest request = createValidRequest();
            request.setTraineeUsername("valid.username");

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainee Password Validation Tests")
    class TraineePasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainee password is blank or null")
        void shouldFailValidationWhenTraineePasswordIsBlankOrNull(String traineePassword) {
            AddTrainingRequest request = createValidRequest();
            request.setTraineePassword(traineePassword);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainee password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid trainee password")
        void shouldPassValidationWithValidTraineePassword() {
            AddTrainingRequest request = createValidRequest();
            request.setTraineePassword("validPassword123");

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainer Username Validation Tests")
    class TrainerUsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainer username is blank or null")
        void shouldFailValidationWhenTrainerUsernameIsBlankOrNull(String trainerUsername) {
            AddTrainingRequest request = createValidRequest();
            request.setTrainerUsername(trainerUsername);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainer username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid trainer username")
        void shouldPassValidationWithValidTrainerUsername() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainerUsername("valid.trainer");

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainer Password Validation Tests")
    class TrainerPasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainer password is blank or null")
        void shouldFailValidationWhenTrainerPasswordIsBlankOrNull(String trainerPassword) {
            AddTrainingRequest request = createValidRequest();
            request.setTrainerPassword(trainerPassword);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainer password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid trainer password")
        void shouldPassValidationWithValidTrainerPassword() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainerPassword("validPassword123");

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Training Name Validation Tests")
    class TrainingNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when training name is blank or null")
        void shouldFailValidationWhenTrainingNameIsBlankOrNull(String trainingName) {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingName(trainingName);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid training name")
        void shouldPassValidationWithValidTrainingName() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingName("Advanced Cardio Training");

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long training name")
        void shouldPassValidationWithLongTrainingName() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingName("A".repeat(200));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Training Date Validation Tests")
    class TrainingDateValidationTests {

        @Test
        @DisplayName("Should fail validation when training date is null")
        void shouldFailValidationWhenTrainingDateIsNull() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(null);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training date is required");
        }

        @Test
        @DisplayName("Should pass validation with valid training date")
        void shouldPassValidationWithValidTrainingDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.of(2024, 12, 25));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Training Duration Validation Tests")
    class TrainingDurationValidationTests {

        @Test
        @DisplayName("Should fail validation when training duration is null")
        void shouldFailValidationWhenTrainingDurationIsNull() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(null);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training duration is required");
        }

        @Test
        @DisplayName("Should fail validation when training duration is zero")
        void shouldFailValidationWhenTrainingDurationIsZero() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(0);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training duration must be positive");
        }

        @Test
        @DisplayName("Should fail validation when training duration is negative")
        void shouldFailValidationWhenTrainingDurationIsNegative() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(-10);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training duration must be positive");
        }

        @Test
        @DisplayName("Should pass validation when training duration is positive")
        void shouldPassValidationWhenTrainingDurationIsPositive() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(90);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when multiple fields are invalid")
        void shouldReturnAllViolationsWhenMultipleFieldsAreInvalid() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(null)
                    .traineePassword("")
                    .trainerUsername("   ")
                    .trainerPassword(null)
                    .trainingName("")
                    .trainingDate(null)
                    .trainingDuration(-5)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(7);
        }

        @Test
        @DisplayName("Should return correct violation messages for all invalid fields")
        void shouldReturnCorrectViolationMessagesForAllInvalidFields() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(null)
                    .traineePassword(null)
                    .trainerUsername(null)
                    .trainerPassword(null)
                    .trainingName(null)
                    .trainingDate(null)
                    .trainingDuration(null)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Trainee username is required",
                            "Trainee password is required",
                            "Trainer username is required",
                            "Trainer password is required",
                            "Training name is required",
                            "Training date is required",
                            "Training duration is required"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("trainee.user")
                    .traineePassword("traineePass")
                    .trainerUsername("trainer.user")
                    .trainerPassword("trainerPass")
                    .trainingName("Test Training")
                    .trainingDate(LocalDate.of(2024, 1, 15))
                    .trainingDuration(45)
                    .build();

            assertThat(request.getTraineeUsername()).isEqualTo("trainee.user");
            assertThat(request.getTraineePassword()).isEqualTo("traineePass");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.user");
            assertThat(request.getTrainerPassword()).isEqualTo("trainerPass");
            assertThat(request.getTrainingName()).isEqualTo("Test Training");
            assertThat(request.getTrainingDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(request.getTrainingDuration()).isEqualTo(45);
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            AddTrainingRequest request = new AddTrainingRequest();

            assertThat(request.getTraineeUsername()).isNull();
            assertThat(request.getTraineePassword()).isNull();
            assertThat(request.getTrainerUsername()).isNull();
            assertThat(request.getTrainerPassword()).isNull();
            assertThat(request.getTrainingName()).isNull();
            assertThat(request.getTrainingDate()).isNull();
            assertThat(request.getTrainingDuration()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            AddTrainingRequest request = new AddTrainingRequest(
                    "trainee.user",
                    "traineePass",
                    "trainer.user",
                    "trainerPass",
                    "Test Training",
                    LocalDate.of(2024, 1, 15),
                    45
            );

            assertThat(request.getTraineeUsername()).isEqualTo("trainee.user");
            assertThat(request.getTraineePassword()).isEqualTo("traineePass");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.user");
            assertThat(request.getTrainerPassword()).isEqualTo("trainerPass");
            assertThat(request.getTrainingName()).isEqualTo("Test Training");
            assertThat(request.getTrainingDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(request.getTrainingDuration()).isEqualTo(45);
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            AddTrainingRequest request = new AddTrainingRequest();

            request.setTraineeUsername("trainee.user");
            request.setTraineePassword("traineePass");
            request.setTrainerUsername("trainer.user");
            request.setTrainerPassword("trainerPass");
            request.setTrainingName("Test Training");
            request.setTrainingDate(LocalDate.of(2024, 1, 15));
            request.setTrainingDuration(45);

            assertThat(request.getTraineeUsername()).isEqualTo("trainee.user");
            assertThat(request.getTraineePassword()).isEqualTo("traineePass");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.user");
            assertThat(request.getTrainerPassword()).isEqualTo("trainerPass");
            assertThat(request.getTrainingName()).isEqualTo("Test Training");
            assertThat(request.getTrainingDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(request.getTrainingDuration()).isEqualTo(45);
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            AddTrainingRequest request1 = createValidRequest();
            AddTrainingRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals implementation for different objects")
        void shouldHaveCorrectEqualsImplementationForDifferentObjects() {
            AddTrainingRequest request1 = createValidRequest();
            AddTrainingRequest request2 = createValidRequest();
            request2.setTrainingName("Different Training");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            AddTrainingRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("AddTrainingRequest");
            assertThat(toString).contains("traineeUsername=John.Doe");
            assertThat(toString).contains("trainerUsername=Alice.Smith");
            assertThat(toString).contains("trainingName=Morning Yoga Session");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("a")
                    .traineePassword("b")
                    .trainerUsername("c")
                    .trainerPassword("d")
                    .trainingName("e")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(1)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with special characters in fields")
        void shouldPassValidationWithSpecialCharactersInFields() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe@test")
                    .traineePassword("P@$$w0rd!")
                    .trainerUsername("alice_smith-123")
                    .trainerPassword("Tr@iner#Pass")
                    .trainingName("Morning Yoga - Advanced (Level 3)")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("josé.müller")
                    .traineePassword("пароль123")
                    .trainerUsername("田中.太郎")
                    .trainerPassword("密码456")
                    .trainingName("Йога для начинающих")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with max integer duration")
        void shouldPassValidationWithMaxIntegerDuration() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDuration(Integer.MAX_VALUE);

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with far future date")
        void shouldPassValidationWithFarFutureDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.of(2099, 12, 31));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with far past date")
        void shouldPassValidationWithFarPastDate() {
            AddTrainingRequest request = createValidRequest();
            request.setTrainingDate(LocalDate.of(1900, 1, 1));

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}