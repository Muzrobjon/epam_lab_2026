package com.epam.gym.trainerworkloadservice.service;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadService service;

    private TrainerWorkloadRequest request;
    private TrainerWorkload existing;
    private final String txId = "test-tx-1";

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        existing = TrainerWorkload.builder()
                .id("id-1")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create new workload when trainer not exists")
    void processWorkload_shouldCreateNew_whenNotExists() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerWorkload.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request, txId);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());

        TrainerWorkload saved = captor.getValue();
        assertThat(saved.getTrainerUsername()).isEqualTo("john.doe");
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getYear()).isEqualTo(2024);
        assertThat(saved.getYears().get(0).getMonths()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(6);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(90L);
    }

    @Test
    @DisplayName("Should update names when trainer already exists")
    void processWorkload_shouldUpdateNames_whenExists() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        request.setTrainerFirstName("UpdatedFirst");
        request.setTrainerLastName("UpdatedLast");

        service.processWorkload(request, txId);

        assertThat(existing.getTrainerFirstName()).isEqualTo("UpdatedFirst");
        assertThat(existing.getTrainerLastName()).isEqualTo("UpdatedLast");
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("Should accumulate duration on multiple ADD calls")
    void processWorkload_shouldAccumulate_onMultipleAdd() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request, txId);
        service.processWorkload(request, txId);

        verify(repository, times(2)).save(existing);
        long total = existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(total).isEqualTo(180L);
    }

    @Test
    @DisplayName("Should subtract duration when DELETE action")
    void processWorkload_shouldSubtract_onDelete() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request, txId);

        TrainerWorkloadRequest deleteReq = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(30)
                .actionType(ActionType.DELETE)
                .build();

        service.processWorkload(deleteReq, txId);

        long total = existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(total).isEqualTo(60L);
    }

    @Test
    @DisplayName("Should not go below zero when subtracting too much")
    void processWorkload_shouldNotGoBelowZero() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request, txId);

        TrainerWorkloadRequest deleteReq = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(500)
                .actionType(ActionType.DELETE)
                .build();

        service.processWorkload(deleteReq, txId);

        long total = existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration();
        assertThat(total).isZero();
    }

    @Test
    @DisplayName("Should create new year entry when year not exists")
    void processWorkload_shouldCreateNewYear() {
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processWorkload(request, txId);

        TrainerWorkloadRequest nextYear = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 6, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        service.processWorkload(nextYear, txId);

        assertThat(existing.getYears()).hasSize(2);
    }

    @Test
    @DisplayName("Should return empty years list when trainer not found")
    void getTrainerWorkload_shouldReturnEmpty_whenNotFound() {
        when(repository.findByTrainerUsername("unknown")).thenReturn(Optional.empty());

        TrainerWorkloadResponse response = service.getTrainerWorkload("unknown", 2024, 6, txId);

        assertThat(response.getTrainerUsername()).isEqualTo("unknown");
        assertThat(response.getYears()).isEmpty();
    }

    @Test
    @DisplayName("Should filter by year and month")
    void getTrainerWorkload_shouldFilterByYearAndMonth() {
        TrainerWorkload.MonthSummary m6 = TrainerWorkload.MonthSummary.builder()
                .month(6).trainingSummaryDuration(90L).build();
        TrainerWorkload.MonthSummary m7 = TrainerWorkload.MonthSummary.builder()
                .month(7).trainingSummaryDuration(60L).build();
        TrainerWorkload.YearSummary y = TrainerWorkload.YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>(List.of(m6, m7)))
                .build();
        existing.setYears(new ArrayList<>(List.of(y)));

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadResponse response = service.getTrainerWorkload("john.doe", 2024, 6, txId);

        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getMonths()).hasSize(1);
        assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(6);
        assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(90L);
    }

    @Test
    @DisplayName("Should return all data when no filters provided")
    void getTrainerWorkload_shouldReturnAll_whenNoFilters() {
        TrainerWorkload.MonthSummary m6 = TrainerWorkload.MonthSummary.builder()
                .month(6).trainingSummaryDuration(90L).build();
        TrainerWorkload.YearSummary y = TrainerWorkload.YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>(List.of(m6)))
                .build();
        existing.setYears(new ArrayList<>(List.of(y)));

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));

        TrainerWorkloadResponse response = service.getTrainerWorkload("john.doe", null, null, txId);

        assertThat(response.getYears()).hasSize(1);
        assertThat(response.getYears().get(0).getMonths()).hasSize(1);
    }
}