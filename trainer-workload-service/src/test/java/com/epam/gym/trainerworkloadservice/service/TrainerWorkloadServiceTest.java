package com.epam.gym.trainerworkloadservice.service;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.entity.Trainer;
import com.epam.gym.trainerworkloadservice.entity.TrainingMonth;
import com.epam.gym.trainerworkloadservice.entity.TrainingYear;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.mapper.TrainerWorkloadMapper;
import com.epam.gym.trainerworkloadservice.repository.TrainerRepository;
import com.epam.gym.trainerworkloadservice.repository.TrainingMonthRepository;
import com.epam.gym.trainerworkloadservice.repository.TrainingYearRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerWorkloadService Unit Tests")
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingYearRepository yearRepository;

    @Mock
    private TrainingMonthRepository monthRepository;

    @Mock
    private TrainerWorkloadMapper mapper;

    @InjectMocks
    private TrainerWorkloadService service;

    private TrainerWorkloadRequest validRequest;
    private Trainer existingTrainer;
    private TrainingYear existingYear;
    private TrainingMonth existingMonth;

    private static final String TRANSACTION_ID = "tx-test-001";

    @BeforeEach
    void setUp() {
        validRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 3, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        existingTrainer = Trainer.builder()
                .id(1L)
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .status(true)
                .years(new ArrayList<>())
                .build();

        existingYear = TrainingYear.builder()
                .id(10L)
                .trainer(existingTrainer)
                .year(2024)
                .months(new ArrayList<>())
                .build();

        existingMonth = TrainingMonth.builder()
                .id(100L)
                .trainingYear(existingYear)
                .month(3)
                .duration(120L)
                .build();
    }

    // ==================== processWorkload() Tests ====================

    @Nested
    @DisplayName("processWorkload()")
    class ProcessWorkloadTests {

        @Test
        @DisplayName("ADD action — new trainer: creates trainer, year, month")
        void processWorkload_AddAction_NewTrainer_CreatesAll() {
            // Given
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.empty());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(yearRepository.findByTrainerAndYear(any(Trainer.class), eq(2024)))
                    .thenReturn(Optional.empty());
            when(yearRepository.save(any(TrainingYear.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthRepository.findByTrainingYearAndMonth(any(TrainingYear.class), eq(3)))
                    .thenReturn(Optional.empty());
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then
            ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
            verify(trainerRepository).save(trainerCaptor.capture());
            assertThat(trainerCaptor.getValue().getUsername()).isEqualTo("john.doe");
            assertThat(trainerCaptor.getValue().getFirstName()).isEqualTo("John");
            assertThat(trainerCaptor.getValue().getLastName()).isEqualTo("Doe");
            assertThat(trainerCaptor.getValue().getStatus()).isTrue();

            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(60L);
            assertThat(monthCaptor.getValue().getMonth()).isEqualTo(3);
        }

        @Test
        @DisplayName("ADD action — existing trainer: increments duration")
        void processWorkload_AddAction_ExistingTrainer_IncrementsDuration() {
            // Given
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then: 120 + 60 = 180
            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(180L);
        }

        @Test
        @DisplayName("DELETE action — existing trainer: decrements duration")
        void processWorkload_DeleteAction_ExistingTrainer_DecrementsDuration() {
            // Given
            validRequest.setActionType(ActionType.DELETE);
            validRequest.setTrainingDuration(40);

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then: 120 - 40 = 80
            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(80L);
        }

        @Test
        @DisplayName("DELETE action — duration goes below zero: clamped to 0")
        void processWorkload_DeleteAction_NegativeResult_ClampedToZero() {
            // Given
            validRequest.setActionType(ActionType.DELETE);
            validRequest.setTrainingDuration(500); // greater than existing 120

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then: clamp to 0
            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(0L);
        }

        @Test
        @DisplayName("ADD action — duration exceeds MAX_MONTHLY_MINUTES: clamped to max")
        void processWorkload_AddAction_ExceedsMax_ClampedToMax() {
            // Given
            existingMonth.setDuration(44000L); // close to max (44640)
            validRequest.setTrainingDuration(1000); // would make 45000, but max is 44640

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then: clamped to 44640
            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(44640L);
        }

        @Test
        @DisplayName("Updates trainer info on every request (name, status)")
        void processWorkload_UpdatesTrainerInfo() {
            // Given
            existingTrainer.setFirstName("OldFirst");
            existingTrainer.setLastName("OldLast");
            existingTrainer.setStatus(false);

            validRequest.setTrainerFirstName("NewFirst");
            validRequest.setTrainerLastName("NewLast");
            validRequest.setIsActive(true);

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
            when(yearRepository.findByTrainerAndYear(any(), anyInt())).thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(any(), anyInt()))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then
            ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
            verify(trainerRepository).save(trainerCaptor.capture());
            assertThat(trainerCaptor.getValue().getFirstName()).isEqualTo("NewFirst");
            assertThat(trainerCaptor.getValue().getLastName()).isEqualTo("NewLast");
            assertThat(trainerCaptor.getValue().getStatus()).isTrue();
        }

        @Test
        @DisplayName("Creates new TrainingYear if not exists")
        void processWorkload_NewYear_CreatesYear() {
            // Given
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.empty());
            when(yearRepository.save(any(TrainingYear.class))).thenAnswer(inv -> inv.getArgument(0));
            when(monthRepository.findByTrainingYearAndMonth(any(), anyInt()))
                    .thenReturn(Optional.empty());
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then
            ArgumentCaptor<TrainingYear> yearCaptor = ArgumentCaptor.forClass(TrainingYear.class);
            verify(yearRepository).save(yearCaptor.capture());
            assertThat(yearCaptor.getValue().getYear()).isEqualTo(2024);
            assertThat(yearCaptor.getValue().getTrainer()).isEqualTo(existingTrainer);
        }

        @Test
        @DisplayName("Creates new TrainingMonth with initial duration 0")
        void processWorkload_NewMonth_StartsFromZero() {
            // Given
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.empty()); // new month
            when(monthRepository.save(any(TrainingMonth.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            service.processWorkload(validRequest, TRANSACTION_ID); // ADD 60

            // Then: 0 + 60 = 60
            ArgumentCaptor<TrainingMonth> monthCaptor = ArgumentCaptor.forClass(TrainingMonth.class);
            verify(monthRepository).save(monthCaptor.capture());
            assertThat(monthCaptor.getValue().getDuration()).isEqualTo(60L);
            assertThat(monthCaptor.getValue().getMonth()).isEqualTo(3);
        }

        @Test
        @DisplayName("Verifies all repositories called exactly once")
        void processWorkload_AllRepositoriesCalledOnce() {
            // Given
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);
            when(yearRepository.findByTrainerAndYear(existingTrainer, 2024))
                    .thenReturn(Optional.of(existingYear));
            when(yearRepository.save(any(TrainingYear.class))).thenReturn(existingYear);
            when(monthRepository.findByTrainingYearAndMonth(existingYear, 3))
                    .thenReturn(Optional.of(existingMonth));
            when(monthRepository.save(any(TrainingMonth.class))).thenReturn(existingMonth);

            // When
            service.processWorkload(validRequest, TRANSACTION_ID);

            // Then
            verify(trainerRepository, times(1)).findByUsername("john.doe");
            verify(trainerRepository, times(1)).save(any(Trainer.class));
            verify(yearRepository, times(1)).findByTrainerAndYear(existingTrainer, 2024);
            verify(yearRepository, times(1)).save(any(TrainingYear.class));
            verify(monthRepository, times(1)).findByTrainingYearAndMonth(existingYear, 3);
            verify(monthRepository, times(1)).save(any(TrainingMonth.class));
        }
    }

    // ==================== getTrainerWorkload() Tests ====================

    @Nested
    @DisplayName("getTrainerWorkload()")
    class GetTrainerWorkloadTests {

        @Test
        @DisplayName("Trainer not found: returns empty response")
        void getTrainerWorkload_TrainerNotFound_ReturnsEmpty() {
            // Given
            when(trainerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "unknown", null, null, TRANSACTION_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTrainerUsername()).isEqualTo("unknown");
            assertThat(response.getYears()).isEmpty();
            assertThat(response.getTrainerFirstName()).isNull();
            verify(trainerRepository, times(1)).findByUsername("unknown");
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Trainer found: returns all years and months (no filters)")
        void getTrainerWorkload_NoFilters_ReturnsAll() {
            // Given
            TrainingMonth march = TrainingMonth.builder()
                    .month(3).duration(120L).trainingYear(existingYear).build();
            TrainingMonth april = TrainingMonth.builder()
                    .month(4).duration(90L).trainingYear(existingYear).build();
            existingYear.setMonths(List.of(march, april));
            existingTrainer.setYears(List.of(existingYear));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            TrainerWorkloadResponse.MonthSummary marchSum = TrainerWorkloadResponse.MonthSummary.builder()
                    .month(3).trainingSummaryDuration(120L).build();
            TrainerWorkloadResponse.MonthSummary aprilSum = TrainerWorkloadResponse.MonthSummary.builder()
                    .month(4).trainingSummaryDuration(90L).build();

            when(mapper.toMonthSummary(march)).thenReturn(marchSum);
            when(mapper.toMonthSummary(april)).thenReturn(aprilSum);

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", null, null, TRANSACTION_ID);

            // Then
            assertThat(response.getTrainerUsername()).isEqualTo("john.doe");
            assertThat(response.getTrainerFirstName()).isEqualTo("John");
            assertThat(response.getTrainerLastName()).isEqualTo("Doe");
            assertThat(response.getTrainerStatus()).isTrue();
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getYear()).isEqualTo(2024);
            assertThat(response.getYears().get(0).getMonths()).hasSize(2);
        }

        @Test
        @DisplayName("Filter by year: returns only matching year")
        void getTrainerWorkload_FilterByYear_ReturnsOnlyThatYear() {
            // Given
            TrainingYear year2023 = TrainingYear.builder()
                    .year(2023).months(new ArrayList<>()).trainer(existingTrainer).build();
            TrainingYear year2024 = TrainingYear.builder()
                    .year(2024).months(new ArrayList<>()).trainer(existingTrainer).build();
            existingTrainer.setYears(List.of(year2023, year2024));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", 2024, null, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Filter by year and month: returns only matching month")
        void getTrainerWorkload_FilterByYearAndMonth_ReturnsOnlyThatMonth() {
            // Given
            TrainingMonth march = TrainingMonth.builder()
                    .month(3).duration(120L).trainingYear(existingYear).build();
            TrainingMonth april = TrainingMonth.builder()
                    .month(4).duration(90L).trainingYear(existingYear).build();
            existingYear.setMonths(List.of(march, april));
            existingTrainer.setYears(List.of(existingYear));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            TrainerWorkloadResponse.MonthSummary marchSum = TrainerWorkloadResponse.MonthSummary.builder()
                    .month(3).trainingSummaryDuration(120L).build();
            when(mapper.toMonthSummary(march)).thenReturn(marchSum);

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", 2024, 3, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(3);
            verify(mapper, times(1)).toMonthSummary(march);
            verify(mapper, never()).toMonthSummary(april);
        }

        @Test
        @DisplayName("Filter by month only: filters across all years")
        void getTrainerWorkload_FilterByMonthOnly_FiltersAcrossYears() {
            // Given
            TrainingMonth march2024 = TrainingMonth.builder()
                    .month(3).duration(120L).build();
            TrainingMonth april2024 = TrainingMonth.builder()
                    .month(4).duration(90L).build();

            TrainingYear year2024 = TrainingYear.builder()
                    .year(2024).months(List.of(march2024, april2024))
                    .trainer(existingTrainer).build();

            existingTrainer.setYears(List.of(year2024));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            TrainerWorkloadResponse.MonthSummary marchSum = TrainerWorkloadResponse.MonthSummary.builder()
                    .month(3).trainingSummaryDuration(120L).build();
            when(mapper.toMonthSummary(march2024)).thenReturn(marchSum);

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", null, 3, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(3);
        }

        @Test
        @DisplayName("Year filter with no match: returns empty years list")
        void getTrainerWorkload_YearNoMatch_ReturnsEmptyYears() {
            // Given
            TrainingYear year2024 = TrainingYear.builder()
                    .year(2024).months(new ArrayList<>()).trainer(existingTrainer).build();
            existingTrainer.setYears(List.of(year2024));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", 2030, null, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).isEmpty();
        }

        @Test
        @DisplayName("Month filter with no match: returns year with empty months")
        void getTrainerWorkload_MonthNoMatch_ReturnsYearWithEmptyMonths() {
            // Given
            TrainingMonth march = TrainingMonth.builder().month(3).duration(120L).build();
            existingYear.setMonths(List.of(march));
            existingTrainer.setYears(List.of(existingYear));

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", 2024, 12, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).isEmpty();
            verify(mapper, never()).toMonthSummary(any());
        }

        @Test
        @DisplayName("Trainer with no years: returns empty years list")
        void getTrainerWorkload_TrainerWithNoYears_ReturnsEmptyList() {
            // Given
            existingTrainer.setYears(new ArrayList<>());
            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", null, null, TRANSACTION_ID);

            // Then
            assertThat(response.getYears()).isEmpty();
            assertThat(response.getTrainerUsername()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Returns trainer info correctly")
        void getTrainerWorkload_ReturnsTrainerInfo() {
            // Given
            existingTrainer.setStatus(false);
            existingTrainer.setFirstName("Jane");
            existingTrainer.setLastName("Smith");
            existingTrainer.setYears(new ArrayList<>());

            when(trainerRepository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            // When
            TrainerWorkloadResponse response = service.getTrainerWorkload(
                    "john.doe", null, null, TRANSACTION_ID);

            // Then
            assertThat(response.getTrainerFirstName()).isEqualTo("Jane");
            assertThat(response.getTrainerLastName()).isEqualTo("Smith");
            assertThat(response.getTrainerStatus()).isFalse();
        }
    }
}