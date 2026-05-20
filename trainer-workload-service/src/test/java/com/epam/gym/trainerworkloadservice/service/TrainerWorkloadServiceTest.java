package com.epam.gym.trainerworkloadservice.service;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload.MonthSummary;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload.YearSummary;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadService service;

    @Captor
    private ArgumentCaptor<TrainerWorkload> workloadCaptor;

    private static final String TRANSACTION_ID = "test-txn-123";
    private static final String USERNAME = "John.Doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private TrainerWorkloadRequest buildRequest(ActionType actionType, LocalDate date, Integer duration) {
        return TrainerWorkloadRequest.builder()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(date)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();
    }

    private TrainerWorkload buildExistingWorkload(int year, int month, long duration) {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(month)
                .trainingSummaryDuration(duration)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(year)
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        return TrainerWorkload.builder()
                .id("existing-id")
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .trainerStatus(true)
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();
    }

    @Nested
    class ProcessWorkload_NewTrainer {

        @Test
        void shouldCreateNewWorkload_WhenTrainerNotFound_AndActionIsAdd() {
            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2026, 5, 11), 60);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getTrainerUsername()).isEqualTo(USERNAME);
            assertThat(saved.getTrainerFirstName()).isEqualTo(FIRST_NAME);
            assertThat(saved.getTrainerLastName()).isEqualTo(LAST_NAME);
            assertThat(saved.getTrainerStatus()).isTrue();
            assertThat(saved.getYears()).hasSize(1);
            assertThat(saved.getYears().get(0).getYear()).isEqualTo(2026);
            assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
            assertThat(saved.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(5);
            assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(60L);
        }

        @Test
        void shouldCreateNewWorkload_WithZeroDuration_WhenActionIsDelete() {
            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, LocalDate.of(2026, 3, 15), 30);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(0L);
        }
    }

    @Nested
    class ProcessWorkload_ExistingTrainer {

        @Test
        void shouldAddDuration_WhenActionIsAdd_AndMonthExists() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2026, 5, 20), 50);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(150L);
        }

        @Test
        void shouldSubtractDuration_WhenActionIsDelete() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, LocalDate.of(2026, 5, 20), 30);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(70L);
        }

        @Test
        void shouldNotGoBelowZero_WhenDeleteExceedsCurrentDuration() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 20L);
            TrainerWorkloadRequest request = buildRequest(ActionType.DELETE, LocalDate.of(2026, 5, 20), 50);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(0L);
        }

        @Test
        void shouldCreateNewMonth_WhenMonthNotExists() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2026, 6, 10), 45);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears().get(0).getMonths()).hasSize(2);
            assertThat(saved.getYears().get(0).getMonths().get(1).getMonth()).isEqualTo(6);
            assertThat(saved.getYears().get(0).getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(45L);
        }

        @Test
        void shouldCreateNewYear_WhenYearNotExists() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2027, 1, 15), 90);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getYears()).hasSize(2);
            assertThat(saved.getYears().get(1).getYear()).isEqualTo(2027);
            assertThat(saved.getYears().get(1).getMonths().get(0).getMonth()).isEqualTo(1);
            assertThat(saved.getYears().get(1).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(90L);
        }

        @Test
        void shouldSyncTrainerIdentity_WhenUpdating() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            existing.setTrainerFirstName("OldFirst");
            existing.setTrainerLastName("OldLast");
            existing.setTrainerStatus(false);

            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2026, 5, 20), 10);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));
            when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

            service.processWorkload(request, TRANSACTION_ID);

            verify(repository).save(workloadCaptor.capture());
            TrainerWorkload saved = workloadCaptor.getValue();

            assertThat(saved.getTrainerFirstName()).isEqualTo(FIRST_NAME);
            assertThat(saved.getTrainerLastName()).isEqualTo(LAST_NAME);
            assertThat(saved.getTrainerStatus()).isTrue();
        }
    }

    @Nested
    class ProcessWorkload_ErrorHandling {

        @Test
        void shouldThrowException_WhenRepositoryFails() {
            TrainerWorkloadRequest request = buildRequest(ActionType.ADD, LocalDate.of(2026, 5, 11), 60);

            when(repository.findByTrainerUsername(USERNAME)).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> service.processWorkload(request, TRANSACTION_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB error");
        }
    }

    @Nested
    class GetTrainerWorkload {

        @Test
        void shouldReturnEmptyResponse_WhenTrainerNotFound() {
            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.empty());

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, null, null, TRANSACTION_ID);

            assertThat(response.getTrainerUsername()).isEqualTo(USERNAME);
            assertThat(response.getTrainerFirstName()).isNull();
            assertThat(response.getTrainerLastName()).isNull();
            assertThat(response.getTrainerStatus()).isNull();
            assertThat(response.getYears()).isEmpty();
        }

        @Test
        void shouldReturnFullResponse_WhenTrainerFound() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, null, null, TRANSACTION_ID);

            assertThat(response.getTrainerUsername()).isEqualTo(USERNAME);
            assertThat(response.getTrainerFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getTrainerLastName()).isEqualTo(LAST_NAME);
            assertThat(response.getTrainerStatus()).isTrue();
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getYear()).isEqualTo(2026);
        }

        @Test
        void shouldFilterByYear() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            YearSummary year2027 = YearSummary.builder()
                    .year(2027)
                    .months(new ArrayList<>(List.of(
                            MonthSummary.builder().month(1).trainingSummaryDuration(50L).build())))
                    .build();
            existing.getYears().add(year2027);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, 2027, null, TRANSACTION_ID);

            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getYear()).isEqualTo(2027);
        }

        @Test
        void shouldFilterByMonth() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);
            existing.getYears().get(0).getMonths().add(
                    MonthSummary.builder().month(6).trainingSummaryDuration(75L).build());

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, 2026, 6, TRANSACTION_ID);

            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(6);
            assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(75L);
        }

        @Test
        void shouldFilterByYearAndMonth() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, 2026, 5, TRANSACTION_ID);

            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(100L);
        }

        @Test
        void shouldReturnEmptyYears_WhenYearFilterDoesNotMatch() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, 2030, null, TRANSACTION_ID);

            assertThat(response.getYears()).isEmpty();
        }

        @Test
        void shouldReturnEmptyMonths_WhenMonthFilterDoesNotMatch() {
            TrainerWorkload existing = buildExistingWorkload(2026, 5, 100L);

            when(repository.findByTrainerUsername(USERNAME)).thenReturn(Optional.of(existing));

            TrainerWorkloadResponse response = service.getTrainerWorkload(USERNAME, 2026, 12, TRANSACTION_ID);

            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths()).isEmpty();
        }
    }
}