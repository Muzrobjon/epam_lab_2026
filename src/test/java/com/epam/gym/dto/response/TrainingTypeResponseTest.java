package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainingTypeResponse Tests")
class TrainingTypeResponseTest {

    private static final Long ID = 1L;
    private static final TrainingTypeName TRAINING_TYPE_NAME = TrainingTypeName.YOGA;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with no-args constructor")
        void shouldCreateInstanceWithNoArgsConstructor() {
            TrainingTypeResponse response = new TrainingTypeResponse();

            assertNotNull(response);
            assertNull(response.getId());
            assertNull(response.getTrainingTypeName());
        }

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateInstanceWithAllArgsConstructor() {
            TrainingTypeResponse response = new TrainingTypeResponse(ID, TRAINING_TYPE_NAME);

            assertAll(
                    () -> assertEquals(ID, response.getId()),
                    () -> assertEquals(TRAINING_TYPE_NAME, response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Should create instance with null values in all-args constructor")
        void shouldCreateInstanceWithNullValuesInAllArgsConstructor() {
            TrainingTypeResponse response = new TrainingTypeResponse(null, null);

            assertAll(
                    () -> assertNull(response.getId()),
                    () -> assertNull(response.getTrainingTypeName())
            );
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create instance using builder with all fields")
        void shouldCreateInstanceUsingBuilderWithAllFields() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(ID)
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            assertAll(
                    () -> assertEquals(ID, response.getId()),
                    () -> assertEquals(TRAINING_TYPE_NAME, response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Should create instance using builder with only id")
        void shouldCreateInstanceUsingBuilderWithOnlyId() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(ID)
                    .build();

            assertAll(
                    () -> assertEquals(ID, response.getId()),
                    () -> assertNull(response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Should create instance using builder with only trainingTypeName")
        void shouldCreateInstanceUsingBuilderWithOnlyTrainingTypeName() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            assertAll(
                    () -> assertNull(response.getId()),
                    () -> assertEquals(TRAINING_TYPE_NAME, response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Should create empty instance using builder")
        void shouldCreateEmptyInstanceUsingBuilder() {
            TrainingTypeResponse response = TrainingTypeResponse.builder().build();

            assertNotNull(response);
            assertNull(response.getId());
            assertNull(response.getTrainingTypeName());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get id")
        void shouldSetAndGetId() {
            TrainingTypeResponse response = new TrainingTypeResponse();
            response.setId(ID);

            assertEquals(ID, response.getId());
        }

        @Test
        @DisplayName("Should set and get trainingTypeName")
        void shouldSetAndGetTrainingTypeName() {
            TrainingTypeResponse response = new TrainingTypeResponse();
            response.setTrainingTypeName(TRAINING_TYPE_NAME);

            assertEquals(TRAINING_TYPE_NAME, response.getTrainingTypeName());
        }

        @Test
        @DisplayName("Should set id to null")
        void shouldSetIdToNull() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(ID)
                    .build();

            response.setId(null);

            assertNull(response.getId());
        }

        @Test
        @DisplayName("Should set trainingTypeName to null")
        void shouldSetTrainingTypeNameToNull() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            response.setTrainingTypeName(null);

            assertNull(response.getTrainingTypeName());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, 100L, Long.MAX_VALUE})
        @DisplayName("Should handle various id values")
        void shouldHandleVariousIdValues(Long id) {
            TrainingTypeResponse response = new TrainingTypeResponse();
            response.setId(id);

            assertEquals(id, response.getId());
        }

        @ParameterizedTest
        @EnumSource(TrainingTypeName.class)
        @DisplayName("Should handle all training type names")
        void shouldHandleAllTrainingTypeNames(TrainingTypeName typeName) {
            TrainingTypeResponse response = new TrainingTypeResponse();
            response.setTrainingTypeName(typeName);

            assertEquals(typeName, response.getTrainingTypeName());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = createFullTrainingTypeResponse();

            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should be equal to itself (reflexive)")
        void shouldBeEqualToItself() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();

            assertEquals(response, response);
        }

        @Test
        @DisplayName("Should be symmetric")
        void shouldBeSymmetric() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = createFullTrainingTypeResponse();

            assertAll(
                    () -> assertEquals(response1, response2),
                    () -> assertEquals(response2, response1)
            );
        }

        @Test
        @DisplayName("Should be transitive")
        void shouldBeTransitive() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = createFullTrainingTypeResponse();
            TrainingTypeResponse response3 = createFullTrainingTypeResponse();

            assertAll(
                    () -> assertEquals(response1, response2),
                    () -> assertEquals(response2, response3),
                    () -> assertEquals(response1, response3)
            );
        }

        @Test
        @DisplayName("Should not be equal for different id")
        void shouldNotBeEqualForDifferentId() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = TrainingTypeResponse.builder()
                    .id(2L)
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal for different trainingTypeName")
        void shouldNotBeEqualForDifferentTrainingTypeName() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = TrainingTypeResponse.builder()
                    .id(ID)
                    .trainingTypeName(TrainingTypeName.FITNESS)
                    .build();

            assertNotEquals(response1, response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();

            assertNotEquals(null, response);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();

            assertNotEquals("string", response);
            assertNotEquals(123, response);
            assertNotEquals(new Object(), response);
        }

        @Test
        @DisplayName("Should have same hashCode for equal objects")
        void shouldHaveSameHashCodeForEqualObjects() {
            TrainingTypeResponse response1 = createFullTrainingTypeResponse();
            TrainingTypeResponse response2 = createFullTrainingTypeResponse();

            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Empty objects should be equal")
        void emptyObjectsShouldBeEqual() {
            TrainingTypeResponse response1 = new TrainingTypeResponse();
            TrainingTypeResponse response2 = new TrainingTypeResponse();

            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should handle null fields in equals")
        void shouldHandleNullFieldsInEquals() {
            TrainingTypeResponse response1 = TrainingTypeResponse.builder()
                    .id(null)
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();
            TrainingTypeResponse response2 = TrainingTypeResponse.builder()
                    .id(ID)
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            assertNotEquals(response1, response2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should contain all field values in toString")
        void shouldContainAllFieldValuesInToString() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();

            String toString = response.toString();

            assertAll(
                    () -> assertThat(toString).contains("TrainingTypeResponse"),
                    () -> assertThat(toString).contains("id"),
                    () -> assertThat(toString).contains(ID.toString()),
                    () -> assertThat(toString).contains("trainingTypeName"),
                    () -> assertThat(toString).contains(TRAINING_TYPE_NAME.toString())
            );
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TrainingTypeResponse response = new TrainingTypeResponse();

            String toString = response.toString();

            assertNotNull(toString);
            assertThat(toString).contains("TrainingTypeResponse");
        }

        @Test
        @DisplayName("Should produce consistent toString output")
        void shouldProduceConsistentToStringOutput() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();

            String toString1 = response.toString();
            String toString2 = response.toString();

            assertEquals(toString1, toString2);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero id")
        void shouldHandleZeroId() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(0L)
                    .build();

            assertEquals(0L, response.getId());
        }

        @Test
        @DisplayName("Should handle negative id")
        void shouldHandleNegativeId() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(-1L)
                    .build();

            assertEquals(-1L, response.getId());
        }

        @Test
        @DisplayName("Should handle Long.MAX_VALUE id")
        void shouldHandleMaxValueId() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(Long.MAX_VALUE)
                    .build();

            assertEquals(Long.MAX_VALUE, response.getId());
        }

        @Test
        @DisplayName("Should handle Long.MIN_VALUE id")
        void shouldHandleMinValueId() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(Long.MIN_VALUE)
                    .build();

            assertEquals(Long.MIN_VALUE, response.getId());
        }

        @ParameterizedTest
        @EnumSource(TrainingTypeName.class)
        @DisplayName("Should create response for each training type")
        void shouldCreateResponseForEachTrainingType(TrainingTypeName typeName) {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id((long) typeName.ordinal() + 1)
                    .trainingTypeName(typeName)
                    .build();

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(typeName, response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Should maintain immutability of enum field")
        void shouldMaintainImmutabilityOfEnumField() {
            TrainingTypeResponse response = createFullTrainingTypeResponse();
            TrainingTypeName originalType = response.getTrainingTypeName();

            response.setTrainingTypeName(TrainingTypeName.FITNESS);

            assertNotEquals(originalType, response.getTrainingTypeName());
            assertEquals(TrainingTypeName.FITNESS, response.getTrainingTypeName());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should support method chaining")
        void shouldSupportMethodChaining() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(ID)
                    .trainingTypeName(TRAINING_TYPE_NAME)
                    .build();

            assertNotNull(response);
        }

        @Test
        @DisplayName("Should allow overwriting values in builder")
        void shouldAllowOverwritingValuesInBuilder() {
            TrainingTypeResponse response = TrainingTypeResponse.builder()
                    .id(1L)
                    .id(2L)
                    .trainingTypeName(TrainingTypeName.YOGA)
                    .trainingTypeName(TrainingTypeName.FITNESS)
                    .build();

            assertAll(
                    () -> assertEquals(2L, response.getId()),
                    () -> assertEquals(TrainingTypeName.FITNESS, response.getTrainingTypeName())
            );
        }

        @Test
        @DisplayName("Builder should create independent instances")
        void builderShouldCreateIndependentInstances() {
            TrainingTypeResponse.TrainingTypeResponseBuilder builder = TrainingTypeResponse.builder()
                    .id(ID)
                    .trainingTypeName(TRAINING_TYPE_NAME);

            TrainingTypeResponse response1 = builder.build();
            TrainingTypeResponse response2 = builder.build();

            assertEquals(response1, response2);
            assertNotSame(response1, response2);
        }
    }

    private TrainingTypeResponse createFullTrainingTypeResponse() {
        return TrainingTypeResponse.builder()
                .id(ID)
                .trainingTypeName(TRAINING_TYPE_NAME)
                .build();
    }
}