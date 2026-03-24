package com.epam.gym.actuator;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymInfoEndpoint Tests")
class GymInfoEndpointTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private GymInfoEndpoint gymInfoEndpoint;

    private static final long TRAINEE_COUNT = 10L;
    private static final long TRAINER_COUNT = 5L;
    private static final long TRAINING_COUNT = 25L;

    @Nested
    @DisplayName("info() method tests")
    class InfoMethodTests {

        @BeforeEach
        void setUp() {
            when(traineeRepository.count()).thenReturn(TRAINEE_COUNT);
            when(trainerRepository.count()).thenReturn(TRAINER_COUNT);
            when(trainingRepository.count()).thenReturn(TRAINING_COUNT);
        }

        @Test
        @DisplayName("Should return application name")
        void shouldReturnApplicationName() {
            Map<String, Object> result = gymInfoEndpoint.info();

            assertThat(result).containsEntry("application", "Gym CRM System");
        }

        @Test
        @DisplayName("Should return version")
        void shouldReturnVersion() {
            Map<String, Object> result = gymInfoEndpoint.info();

            assertThat(result).containsEntry("version", "1.0.0");
        }

        @Test
        @DisplayName("Should return timestamp")
        void shouldReturnTimestamp() {
            LocalDateTime before = LocalDateTime.now();

            Map<String, Object> result = gymInfoEndpoint.info();

            LocalDateTime after = LocalDateTime.now();
            String timestamp = (String) result.get("timestamp");

            assertThat(timestamp).isNotNull();
            LocalDateTime resultTime = LocalDateTime.parse(timestamp);
            assertThat(resultTime).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should return statistics with correct counts")
        @SuppressWarnings("unchecked")
        void shouldReturnStatisticsWithCorrectCounts() {
            Map<String, Object> result = gymInfoEndpoint.info();

            assertThat(result).containsKey("statistics");
            Map<String, Long> statistics = (Map<String, Long>) result.get("statistics");

            assertThat(statistics)
                    .containsEntry("trainees", TRAINEE_COUNT)
                    .containsEntry("trainers", TRAINER_COUNT)
                    .containsEntry("trainings", TRAINING_COUNT);
        }

        @Test
        @DisplayName("Should call all repository count methods")
        void shouldCallAllRepositoryCountMethods() {
            gymInfoEndpoint.info();

            verify(traineeRepository).count();
            verify(trainerRepository).count();
            verify(trainingRepository).count();
        }

        @Test
        @DisplayName("Should return all expected keys")
        void shouldReturnAllExpectedKeys() {
            Map<String, Object> result = gymInfoEndpoint.info();

            assertThat(result).containsKeys("application", "version", "timestamp", "statistics");
        }

        @Test
        @DisplayName("Should handle zero counts")
        @SuppressWarnings("unchecked")
        void shouldHandleZeroCounts() {
            when(traineeRepository.count()).thenReturn(0L);
            when(trainerRepository.count()).thenReturn(0L);
            when(trainingRepository.count()).thenReturn(0L);

            Map<String, Object> result = gymInfoEndpoint.info();
            Map<String, Long> statistics = (Map<String, Long>) result.get("statistics");

            assertThat(statistics)
                    .containsEntry("trainees", 0L)
                    .containsEntry("trainers", 0L)
                    .containsEntry("trainings", 0L);
        }
    }

    @Nested
    @DisplayName("detail() method tests")
    class DetailMethodTests {

        @Test
        @DisplayName("Should return trainee details when name is 'trainees'")
        void shouldReturnTraineeDetails() {
            when(traineeRepository.count()).thenReturn(TRAINEE_COUNT);

            Map<String, Object> result = gymInfoEndpoint.detail("trainees");

            assertThat(result)
                    .containsEntry("entity", "Trainee")
                    .containsEntry("count", TRAINEE_COUNT);
            verify(traineeRepository).count();
        }

        @Test
        @DisplayName("Should return trainer details when name is 'trainers'")
        void shouldReturnTrainerDetails() {
            when(trainerRepository.count()).thenReturn(TRAINER_COUNT);

            Map<String, Object> result = gymInfoEndpoint.detail("trainers");

            assertThat(result)
                    .containsEntry("entity", "Trainer")
                    .containsEntry("count", TRAINER_COUNT);
            verify(trainerRepository).count();
        }

        @Test
        @DisplayName("Should return training details when name is 'trainings'")
        void shouldReturnTrainingDetails() {
            when(trainingRepository.count()).thenReturn(TRAINING_COUNT);

            Map<String, Object> result = gymInfoEndpoint.detail("trainings");

            assertThat(result)
                    .containsEntry("entity", "Training")
                    .containsEntry("count", TRAINING_COUNT);
            verify(trainingRepository).count();
        }

        @ParameterizedTest
        @ValueSource(strings = {"unknown", "invalid", "users", "TRAINEES", "Trainers", ""})
        @DisplayName("Should return error for unknown entity names")
        void shouldReturnErrorForUnknownEntityNames(String unknownName) {
            Map<String, Object> result = gymInfoEndpoint.detail(unknownName);

            assertThat(result)
                    .containsKey("error")
                    .containsEntry("error", "Unknown: " + unknownName);
        }

        @Test
        @DisplayName("Should not call other repositories when querying trainees")
        void shouldNotCallOtherRepositoriesWhenQueryingTrainees() {
            when(traineeRepository.count()).thenReturn(TRAINEE_COUNT);

            gymInfoEndpoint.detail("trainees");

            verify(traineeRepository).count();
            // trainerRepository and trainingRepository should not be called
        }

        @Test
        @DisplayName("Should return only two keys for valid entity")
        void shouldReturnOnlyTwoKeysForValidEntity() {
            when(traineeRepository.count()).thenReturn(TRAINEE_COUNT);

            Map<String, Object> result = gymInfoEndpoint.detail("trainees");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return only one key for unknown entity")
        void shouldReturnOnlyOneKeyForUnknownEntity() {
            Map<String, Object> result = gymInfoEndpoint.detail("unknown");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle large counts")
        @SuppressWarnings("unchecked")
        void shouldHandleLargeCounts() {
            long largeCount = Long.MAX_VALUE;
            when(traineeRepository.count()).thenReturn(largeCount);
            when(trainerRepository.count()).thenReturn(largeCount);
            when(trainingRepository.count()).thenReturn(largeCount);

            Map<String, Object> result = gymInfoEndpoint.info();
            Map<String, Long> statistics = (Map<String, Long>) result.get("statistics");

            assertThat(statistics)
                    .containsEntry("trainees", largeCount)
                    .containsEntry("trainers", largeCount)
                    .containsEntry("trainings", largeCount);
        }
    }
}