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

    // ==================== VALID REQUEST TESTS ====================

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidation_WhenAllFieldsAreValid() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio Session")
                    .trainingDate(LocalDate.now().plusDays(1))
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with minimum valid duration")
        void shouldPassValidation_WhenDurationIsMinimumPositive() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Quick Session")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(1)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== TRAINEE USERNAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("Trainee Username Validation Tests")
    class TraineeUsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainee username is blank or null")
        void shouldFailValidation_WhenTraineeUsernameIsBlankOrNull(String traineeUsername) {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(traineeUsername)
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("traineeUsername"));
        }

        @Test
        @DisplayName("Should have correct error message for blank trainee username")
        void shouldHaveCorrectErrorMessage_WhenTraineeUsernameIsBlank() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainee username is required");
        }
    }

    // ==================== TRAINER USERNAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("Trainer Username Validation Tests")
    class TrainerUsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when trainer username is blank or null")
        void shouldFailValidation_WhenTrainerUsernameIsBlankOrNull(String trainerUsername) {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername(trainerUsername)
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("trainerUsername"));
        }

        @Test
        @DisplayName("Should have correct error message for blank trainer username")
        void shouldHaveCorrectErrorMessage_WhenTrainerUsernameIsBlank() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Trainer username is required");
        }
    }

    // ==================== TRAINING NAME VALIDATION TESTS ====================

    @Nested
    @DisplayName("Training Name Validation Tests")
    class TrainingNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should fail validation when training name is blank or null")
        void shouldFailValidation_WhenTrainingNameIsBlankOrNull(String trainingName) {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName(trainingName)
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("trainingName"));
        }

        @Test
        @DisplayName("Should have correct error message for blank training name")
        void shouldHaveCorrectErrorMessage_WhenTrainingNameIsBlank() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training name is required");
        }
    }

    // ==================== TRAINING DATE VALIDATION TESTS ====================

    @Nested
    @DisplayName("Training Date Validation Tests")
    class TrainingDateValidationTests {

        @Test
        @DisplayName("Should fail validation when training date is null")
        void shouldFailValidation_WhenTrainingDateIsNull() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(null)
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("trainingDate"));
        }

        @Test
        @DisplayName("Should have correct error message for null training date")
        void shouldHaveCorrectErrorMessage_WhenTrainingDateIsNull() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(null)
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training date is required");
        }

        @Test
        @DisplayName("Should pass validation with past date")
        void shouldPassValidation_WhenTrainingDateIsInPast() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now().minusDays(10))
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with future date")
        void shouldPassValidation_WhenTrainingDateIsInFuture() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now().plusDays(30))
                    .trainingDuration(60)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== TRAINING DURATION VALIDATION TESTS ====================

    @Nested
    @DisplayName("Training Duration Validation Tests")
    class TrainingDurationValidationTests {

        @Test
        @DisplayName("Should fail validation when training duration is null")
        void shouldFailValidation_WhenTrainingDurationIsNull() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(null)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("trainingDuration"));
        }

        @Test
        @DisplayName("Should have correct error message for null training duration")
        void shouldHaveCorrectErrorMessage_WhenTrainingDurationIsNull() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(null)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training duration is required");
        }

        @Test
        @DisplayName("Should fail validation when training duration is zero")
        void shouldFailValidation_WhenTrainingDurationIsZero() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(0)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .anyMatch(v -> v.getPropertyPath().toString().equals("trainingDuration"));
        }

        @Test
        @DisplayName("Should fail validation when training duration is negative")
        void shouldFailValidation_WhenTrainingDurationIsNegative() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(-30)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Training duration must be positive");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 30, 60, 90, 120, 180})
        @DisplayName("Should pass validation with positive duration values")
        void shouldPassValidation_WhenTrainingDurationIsPositive(int duration) {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(LocalDate.now())
                    .trainingDuration(duration)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== MULTIPLE VALIDATION ERRORS TESTS ====================

    @Nested
    @DisplayName("Multiple Validation Errors Tests")
    class MultipleValidationErrorsTests {

        @Test
        @DisplayName("Should return all violations when multiple fields are invalid")
        void shouldReturnAllViolations_WhenMultipleFieldsAreInvalid() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("")
                    .trainerUsername(null)
                    .trainingName("   ")
                    .trainingDate(null)
                    .trainingDuration(-1)
                    .build();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
        }

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolations_WhenAllFieldsAreNull() {
            AddTrainingRequest request = new AddTrainingRequest();

            Set<ConstraintViolation<AddTrainingRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder(
                            "traineeUsername",
                            "trainerUsername",
                            "trainingName",
                            "trainingDate",
                            "trainingDuration"
                    );
        }
    }

    // ==================== LOMBOK FUNCTIONALITY TESTS ====================

    @Nested
    @DisplayName("Lombok Functionality Tests")
    class LombokFunctionalityTests {

        @Test
        @DisplayName("Should create object using builder")
        void shouldCreateObject_UsingBuilder() {
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);

            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(trainingDate)
                    .trainingDuration(60)
                    .build();

            assertThat(request.getTraineeUsername()).isEqualTo("john.doe");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.smith");
            assertThat(request.getTrainingName()).isEqualTo("Morning Cardio");
            assertThat(request.getTrainingDate()).isEqualTo(trainingDate);
            assertThat(request.getTrainingDuration()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should create object using no-args constructor and setters")
        void shouldCreateObject_UsingNoArgsConstructorAndSetters() {
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);

            AddTrainingRequest request = new AddTrainingRequest();
            request.setTraineeUsername("john.doe");
            request.setTrainerUsername("trainer.smith");
            request.setTrainingName("Morning Cardio");
            request.setTrainingDate(trainingDate);
            request.setTrainingDuration(60);

            assertThat(request.getTraineeUsername()).isEqualTo("john.doe");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.smith");
            assertThat(request.getTrainingName()).isEqualTo("Morning Cardio");
            assertThat(request.getTrainingDate()).isEqualTo(trainingDate);
            assertThat(request.getTrainingDuration()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should create object using all-args constructor")
        void shouldCreateObject_UsingAllArgsConstructor() {
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);

            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe",
                    "trainer.smith",
                    "Morning Cardio",
                    trainingDate,
                    60
            );

            assertThat(request.getTraineeUsername()).isEqualTo("john.doe");
            assertThat(request.getTrainerUsername()).isEqualTo("trainer.smith");
            assertThat(request.getTrainingName()).isEqualTo("Morning Cardio");
            assertThat(request.getTrainingDate()).isEqualTo(trainingDate);
            assertThat(request.getTrainingDuration()).isEqualTo(60);
        }

        @Test
        @DisplayName("Should have correct equals and hashCode")
        void shouldHaveCorrectEqualsAndHashCode() {
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);

            AddTrainingRequest request1 = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(trainingDate)
                    .trainingDuration(60)
                    .build();

            AddTrainingRequest request2 = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(trainingDate)
                    .trainingDuration(60)
                    .build();

            AddTrainingRequest request3 = AddTrainingRequest.builder()
                    .traineeUsername("jane.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(trainingDate)
                    .trainingDuration(60)
                    .build();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
            assertThat(request1).isNotEqualTo(request3);
        }

        @Test
        @DisplayName("Should have correct toString")
        void shouldHaveCorrectToString() {
            LocalDate trainingDate = LocalDate.of(2024, 6, 15);

            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe")
                    .trainerUsername("trainer.smith")
                    .trainingName("Morning Cardio")
                    .trainingDate(trainingDate)
                    .trainingDuration(60)
                    .build();

            String toString = request.toString();

            assertThat(toString).contains("john.doe");
            assertThat(toString).contains("trainer.smith");
            assertThat(toString).contains("Morning Cardio");
            assertThat(toString).contains("2024-06-15");
            assertThat(toString).contains("60");
        }
    }
}