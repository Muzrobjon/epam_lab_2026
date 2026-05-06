package com.epam.gym.trainerworkloadservice.service;

import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.entity.Trainer;
import com.epam.gym.trainerworkloadservice.entity.TrainingMonth;
import com.epam.gym.trainerworkloadservice.entity.TrainingYear;
import com.epam.gym.trainerworkloadservice.mapper.TrainerWorkloadMapper;
import com.epam.gym.trainerworkloadservice.repository.TrainerRepository;
import com.epam.gym.trainerworkloadservice.repository.TrainingMonthRepository;
import com.epam.gym.trainerworkloadservice.repository.TrainingYearRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private static final long MAX_MONTHLY_MINUTES = 44640L;
    private final TrainerRepository trainerRepository;
    private final TrainingYearRepository yearRepository;
    private final TrainingMonthRepository monthRepository;
    private final TrainerWorkloadMapper mapper;

    @Transactional
    public void processWorkload(TrainerWorkloadRequest request, String transactionId) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        Trainer trainer = trainerRepository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> Trainer.builder()
                        .username(request.getTrainerUsername())
                        .firstName(request.getTrainerFirstName())
                        .lastName(request.getTrainerLastName())
                        .status(request.getIsActive())
                        .build());

        trainer.setFirstName(request.getTrainerFirstName());
        trainer.setLastName(request.getTrainerLastName());
        trainer.setStatus(request.getIsActive());
        trainerRepository.save(trainer);

        TrainingYear trainingYear = yearRepository.findByTrainerAndYear(trainer, year)
                .orElseGet(() -> TrainingYear.builder()
                        .trainer(trainer)
                        .year(year)
                        .build());
        yearRepository.save(trainingYear);

        TrainingMonth trainingMonth = monthRepository
                .findByTrainingYearAndMonth(trainingYear, month)
                .orElseGet(() -> TrainingMonth.builder()
                        .trainingYear(trainingYear)
                        .month(month)
                        .duration(0L)
                        .build());

        int sign = request.getActionType() == ActionType.ADD ? 1 : -1;
        long newDuration = trainingMonth.getDuration() + (sign * request.getTrainingDuration());

        newDuration = Math.max(0, newDuration);                   // no less than 0
        newDuration = Math.min(MAX_MONTHLY_MINUTES, newDuration); // no more than max

        trainingMonth.setDuration(newDuration);
        monthRepository.save(trainingMonth);

        log.info("[TransactionId: {}] Workload processed: trainer={}, year={}, month={}, duration={}",
                transactionId, request.getTrainerUsername(), year, month, newDuration);
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadResponse getTrainerWorkload(
            String username, Integer year, Integer month, String transactionId) {

        log.info("[TransactionId: {}] Fetching workload for trainer: {}", transactionId, username);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElse(null);

        if (trainer == null) {
            log.info("[TransactionId: {}] No trainer found: {}", transactionId, username);
            return TrainerWorkloadResponse.builder()
                    .trainerUsername(username)
                    .years(Collections.emptyList())
                    .build();
        }

        List<TrainerWorkloadResponse.YearSummary> yearSummaries = trainer.getYears().stream()
                .filter(y -> year == null || y.getYear().equals(year))
                .map(y -> {
                    List<TrainerWorkloadResponse.MonthSummary> monthSummaries = y.getMonths().stream()
                            .filter(m -> month == null || m.getMonth().equals(month))
                            .map(m -> mapper.toMonthSummary(m))
                            .toList();

                    return TrainerWorkloadResponse.YearSummary.builder()
                            .year(y.getYear())
                            .months(monthSummaries)
                            .build();
                })
                .toList();

        log.info("[TransactionId: {}] Workload retrieved for trainer: {}", transactionId, username);

        return TrainerWorkloadResponse.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .trainerStatus(trainer.getStatus())
                .years(yearSummaries)
                .build();
    }
}