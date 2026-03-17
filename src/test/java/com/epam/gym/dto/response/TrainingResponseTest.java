package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainingResponse Tests")
class TrainingResponseTest {

    private static final String TRAINING_NAME = "Morning Yoga Session";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 15);
    private static final TrainingTypeName TRAINING_TYPE = TrainingTypeName.YOGA;
    private static final Integer TRAINING_DURATION = 60;
    private static final String TRAINER_NAME = "Alice Smith";
    private static final String TRAINEE_NAME = "John Doe";

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with no-args constructor")
        void shouldCreateInstanceWithNoArgsConstructor() {
            TrainingResponse response = new TrainingResponse();

            assertNotNull(response);
            assertNull(response.getTrainingName());
            assertNull(response.getTrainingDate());
            assertNull(response.getTrainingType());
            assertNull(response.getTrainingDuration());
            assertNull(response.getTrainerName());
            assertNull(response.getTraineeName());
        }

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateInstanceWithAllArgsConstructor() {
            TrainingResponse response = new TrainingResponse(
                    TRAINING_NAME,
                    TRAINING_DATE,
                    TRAINING_TYPE,
                    TRAINING_DURATION,
                    TRAINER_NAME,
                    TRAINEE_NAME
            );

            assertAll(
                    () -> assertEquals(TRAINING_NAME, response.getTrainingName()),
                    () -> assertEquals(TRAINING_DATE, response.getTrainingDate()),
                    () -> assertEquals(TRAINING_TYPE, response.getTrainingType()),
                    () -> assertEquals(TRAINING_DURATION, response.getTrainingDuration()),
                    () -> assertEquals(TRAINER_NAME, response.getTrainerName()),
                    () -> assertEquals(TRAINEE_NAME, response.getTraineeName())
            );
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create instance using builder with all fields")
        void shouldCreateInstanceUsingBuilderWithAllFields() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingType(TRAINING_TYPE)
                    .trainingDuration(TRAINING_DURATION)
                    .trainerName(TRAINER_NAME)
                    .traineeName(TRAINEE_NAME)
                    .build();

            assertAll(
                    () -> assertEquals(TRAINING_NAME, response.getTrainingName()),
                    () -> assertEquals(TRAINING_DATE, response.getTrainingDate()),
                    () -> assertEquals(TRAINING_TYPE, response.getTrainingType()),
                    () -> assertEquals(TRAINING_DURATION, response.getTrainingDuration()),
                    () -> assertEquals(TRAINER_NAME, response.getTrainerName()),
                    () -> assertEquals(TRAINEE_NAME, response.getTraineeName())
            );
        }

        @Test
        @DisplayName("Should create instance using builder with partial fields")
        void shouldCreateInstanceUsingBuilderWithPartialFields() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .build();

            assertAll(
                    () -> assertEquals(TRAINING_NAME, response.getTrainingName()),
                    () -> assertEquals(TRAINING_DATE, response.getTrainingDate()),
                    () -> assertNull(response.getTrainingType()),
                    () -> assertNull(response.getTrainingDuration()),
                    () -> assertNull(response.getTrainerName()),
                    () -> assertNull(response.getTraineeName())
            );
        }

        @Test
        @DisplayName("Should create empty instance using builder")
        void shouldCreateEmptyInstanceUsingBuilder() {
            TrainingResponse response = TrainingResponse.builder().build();

            assertNotNull(response);
            assertNull(response.getTrainingName());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get trainingName")
        void shouldSetAndGetTrainingName() {
            TrainingResponse response = new TrainingResponse();
            response.setTrainingName(TRAINING_NAME);

            assertEquals(TRAINING_NAME, response.getTrainingName());
        }

        @Test
        @DisplayName("Should set and get trainingDate")
        void shouldSetAndGetTrainingDate() {
            TrainingResponse response = new TrainingResponse();
            response.setTrainingDate(TRAINING_DATE);

            assertEquals(TRAINING_DATE, response.getTrainingDate());
        }

        @Test
        @DisplayName("Should set and get trainingType")
        void shouldSetAndGetTrainingType() {
            TrainingResponse response = new TrainingResponse();
            response.setTrainingType(TRAINING_TYPE);

            assertEquals(TRAINING_TYPE, response.getTrainingType());
        }

        @Test
        @DisplayName("Should set and get trainingDuration")
        void shouldSetAndGetTrainingDuration() {
            TrainingResponse response = new TrainingResponse();
            response.setTrainingDuration(TRAINING_DURATION);

            assertEquals(TRAINING_DURATION, response.getTrainingDuration());
        }

        @Test
        @DisplayName("Should set and get trainerName")
        void shouldSetAndGetTrainerName() {
            TrainingResponse response = new TrainingResponse();
            response.setTrainerName(TRAINER_NAME);

            assertEquals(TRAINER_NAME, response.getTrainerName());
        }

        @Test
        @DisplayName("Should set and get traineeName")
        void shouldSetAndGetTraineeName() {
            TrainingResponse response = new TrainingResponse();
            response.setTraineeName(TRAINEE_NAME);

            assertEquals(TRAINEE_NAME, response.getTraineeName());
        }

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingName(TRAINING_NAME)
                    .build();

            response.setTrainingName(null);

            assertNull(response.getTrainingName());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();

            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TrainingResponse response = createFullTrainingResponse();

            assertEquals(response, response);
        }

        @Test
        @DisplayName("Should not be equal for different trainingName")
        void shouldNotBeEqualForDifferentTrainingName() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();
            response2.setTrainingName("Different Training");

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal for different trainingDate")
        void shouldNotBeEqualForDifferentTrainingDate() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();
            response2.setTrainingDate(LocalDate.of(2024, 7, 20));

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal for different trainingType")
        void shouldNotBeEqualForDifferentTrainingType() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();
            response2.setTrainingType(TrainingTypeName.FITNESS);

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal for different trainingDuration")
        void shouldNotBeEqualForDifferentTrainingDuration() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();
            response2.setTrainingDuration(90);

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TrainingResponse response = createFullTrainingResponse();

            assertNotEquals(null, response);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TrainingResponse response = createFullTrainingResponse();

            assertNotEquals("string", response);
        }

        @Test
        @DisplayName("Should have same hashCode for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            TrainingResponse response1 = createFullTrainingResponse();
            TrainingResponse response2 = createFullTrainingResponse();

            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Empty objects should be equal")
        void emptyObjectsShouldBeEqual() {
            TrainingResponse response1 = new TrainingResponse();
            TrainingResponse response2 = new TrainingResponse();

            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field values in toString")
        void shouldContainAllFieldValuesInToString() {
            TrainingResponse response = createFullTrainingResponse();

            String toString = response.toString();

            assertAll(
                    () -> assertThat(toString).contains("trainingName"),
                    () -> assertThat(toString).contains(TRAINING_NAME),
                    () -> assertThat(toString).contains("trainingDate"),
                    () -> assertThat(toString).contains(TRAINING_DATE.toString()),
                    () -> assertThat(toString).contains("trainingType"),
                    () -> assertThat(toString).contains(TRAINING_TYPE.toString()),
                    () -> assertThat(toString).contains("trainingDuration"),
                    () -> assertThat(toString).contains(TRAINING_DURATION.toString()),
                    () -> assertThat(toString).contains("trainerName"),
                    () -> assertThat(toString).contains(TRAINER_NAME),
                    () -> assertThat(toString).contains("traineeName"),
                    () -> assertThat(toString).contains(TRAINEE_NAME)
            );
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TrainingResponse response = new TrainingResponse();

            String toString = response.toString();

            assertNotNull(toString);
            assertThat(toString).contains("TrainingResponse");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string values")
        void shouldHandleEmptyStringValues() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingName("")
                    .trainerName("")
                    .traineeName("")
                    .build();

            assertAll(
                    () -> assertEquals("", response.getTrainingName()),
                    () -> assertEquals("", response.getTrainerName()),
                    () -> assertEquals("", response.getTraineeName())
            );
        }

        @Test
        @DisplayName("Should handle zero duration")
        void shouldHandleZeroDuration() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingDuration(0)
                    .build();

            assertEquals(0, response.getTrainingDuration());
        }

        @Test
        @DisplayName("Should handle negative duration")
        void shouldHandleNegativeDuration() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingDuration(-10)
                    .build();

            assertEquals(-10, response.getTrainingDuration());
        }

        @Test
        @DisplayName("Should handle past date")
        void shouldHandlePastDate() {
            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            TrainingResponse response = TrainingResponse.builder()
                    .trainingDate(pastDate)
                    .build();

            assertEquals(pastDate, response.getTrainingDate());
        }

        @Test
        @DisplayName("Should handle future date")
        void shouldHandleFutureDate() {
            LocalDate futureDate = LocalDate.of(2030, 12, 31);
            TrainingResponse response = TrainingResponse.builder()
                    .trainingDate(futureDate)
                    .build();

            assertEquals(futureDate, response.getTrainingDate());
        }

        @Test
        @DisplayName("Should handle all training types")
        void shouldHandleAllTrainingTypes() {
            for (TrainingTypeName type : TrainingTypeName.values()) {
                TrainingResponse response = TrainingResponse.builder()
                        .trainingType(type)
                        .build();

                assertEquals(type, response.getTrainingType());
            }
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            String specialName = "Training @#$%^&*()!";
            TrainingResponse response = TrainingResponse.builder()
                    .trainingName(specialName)
                    .trainerName("O'Connor-Smith")
                    .traineeName("José García")
                    .build();

            assertAll(
                    () -> assertEquals(specialName, response.getTrainingName()),
                    () -> assertEquals("O'Connor-Smith", response.getTrainerName()),
                    () -> assertEquals("José García", response.getTraineeName())
            );
        }

        @Test
        @DisplayName("Should handle large duration value")
        void shouldHandleLargeDurationValue() {
            TrainingResponse response = TrainingResponse.builder()
                    .trainingDuration(Integer.MAX_VALUE)
                    .build();

            assertEquals(Integer.MAX_VALUE, response.getTrainingDuration());
        }
    }

    private TrainingResponse createFullTrainingResponse() {
        return TrainingResponse.builder()
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingType(TRAINING_TYPE)
                .trainingDuration(TRAINING_DURATION)
                .trainerName(TRAINER_NAME)
                .traineeName(TRAINEE_NAME)
                .build();
    }
}