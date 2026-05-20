package com.epam.gym.trainerworkloadservice.service;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload.MonthSummary;
import com.epam.gym.trainerworkloadservice.entity.TrainerWorkload.YearSummary;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository repository;

    public void processWorkload(TrainerWorkloadRequest request, String transactionId) {
        log.info("[TransactionId: {}] START processWorkload: username={}, action={}, date={}, duration={}",
                transactionId, request.getTrainerUsername(), request.getActionType(),
                request.getTrainingDate(), request.getTrainingDuration());

        try {
            LocalDate trainingDate = request.getTrainingDate();
            int year = trainingDate.getYear();
            int month = trainingDate.getMonthValue();

            TrainerWorkload workload = repository.findByTrainerUsername(request.getTrainerUsername())
                    .map(existing -> {
                        log.debug("[TransactionId: {}] Trainer found (id={}), updating", transactionId, existing.getId());
                        syncTrainerIdentity(existing, request);
                        applyDurationChange(existing, year, month,
                                request.getTrainingDuration(), request.getActionType(), transactionId);
                        return existing;
                    })
                    .orElseGet(() -> {
                        log.debug("[TransactionId: {}] Trainer not found, creating new document", transactionId);
                        return createNewWorkload(request, year, month);
                    });

            TrainerWorkload saved = repository.save(workload);

            log.info("[TransactionId: {}] END processWorkload SUCCESS: documentId={}",
                    transactionId, saved.getId());

        } catch (Exception ex) {
            log.error("[TransactionId: {}] END processWorkload FAILED: {}",
                    transactionId, ex.getMessage(), ex);
            throw ex;
        }
    }

    private TrainerWorkload createNewWorkload(TrainerWorkloadRequest request, int year, int month) {
        long initialDuration = request.getActionType() == ActionType.ADD
                ? request.getTrainingDuration().longValue()
                : 0L;

        MonthSummary monthSummary = MonthSummary.builder()
                .month(month)
                .trainingSummaryDuration(initialDuration)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(year)
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        return TrainerWorkload.builder()
                .trainerUsername(request.getTrainerUsername())
                .trainerFirstName(request.getTrainerFirstName())
                .trainerLastName(request.getTrainerLastName())
                .trainerStatus(request.getIsActive())
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();
    }

    private void syncTrainerIdentity(TrainerWorkload workload, TrainerWorkloadRequest request) {
        workload.setTrainerFirstName(request.getTrainerFirstName());
        workload.setTrainerLastName(request.getTrainerLastName());
        workload.setTrainerStatus(request.getIsActive());
    }

    private void applyDurationChange(TrainerWorkload workload, int year, int month,
                                     Integer trainingDuration, ActionType actionType,
                                     String transactionId) {
        YearSummary yearSummary = findOrCreateYear(workload, year);
        MonthSummary monthSummary = findOrCreateMonth(yearSummary, month);

        long oldDuration = monthSummary.getTrainingSummaryDuration();
        long requestDuration = trainingDuration.longValue();
        long newDuration = (actionType == ActionType.ADD)
                ? oldDuration + requestDuration
                : Math.max(0L, oldDuration - requestDuration);

        monthSummary.setTrainingSummaryDuration(newDuration);

        log.info("[TransactionId: {}] Duration updated [year={}, month={}]: {} -> {} ({})",
                transactionId, year, month, oldDuration, newDuration, actionType);
    }

    private YearSummary findOrCreateYear(TrainerWorkload workload, int year) {
        return workload.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    workload.getYears().add(newYear);
                    return newYear;
                });
    }

    private MonthSummary findOrCreateMonth(YearSummary yearSummary, int month) {
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(month)
                            .trainingSummaryDuration(0L)
                            .build();
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    public TrainerWorkloadResponse getTrainerWorkload(String username, Integer year,
                                                      Integer month, String transactionId) {
        log.info("[TransactionId: {}] START getTrainerWorkload: username={}, year={}, month={}",
                transactionId, username, year, month);

        Optional<TrainerWorkload> opt = repository.findByTrainerUsername(username);

        if (opt.isEmpty()) {
            log.warn("[TransactionId: {}] No workload found for username={}", transactionId, username);
            return TrainerWorkloadResponse.builder()
                    .trainerUsername(username)
                    .years(Collections.emptyList())
                    .build();
        }

        TrainerWorkload workload = opt.get();

        List<TrainerWorkloadResponse.YearSummary> filteredYears = workload.getYears().stream()
                .filter(y -> year == null || y.getYear().equals(year))
                .map(y -> TrainerWorkloadResponse.YearSummary.builder()
                        .year(y.getYear())
                        .months(y.getMonths().stream()
                                .filter(m -> month == null || m.getMonth().equals(month))
                                .map(m -> TrainerWorkloadResponse.MonthSummary.builder()
                                        .month(m.getMonth())
                                        .trainingSummaryDuration(m.getTrainingSummaryDuration())
                                        .build())
                                .toList())
                        .build())
                .toList();

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername(workload.getTrainerUsername())
                .trainerFirstName(workload.getTrainerFirstName())
                .trainerLastName(workload.getTrainerLastName())
                .trainerStatus(workload.getTrainerStatus())
                .years(filteredYears)
                .build();

        log.info("[TransactionId: {}] END getTrainerWorkload SUCCESS: yearsCount={}",
                transactionId, filteredYears.size());

        return response;
    }
}