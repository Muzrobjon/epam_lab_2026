package com.epam.gym.config;

import com.epam.gym.dto.request.TrainerWorkloadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.epam.gym.client.WorkloadServiceClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
@Profile("stg")
@Slf4j
public class TestWorkloadServiceConfig {

    private static final Map<String, Long> trainerWorkloads = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public WorkloadServiceClient mockWorkloadServiceClient() {
        return new WorkloadServiceClient() {
            @Override
            public ResponseEntity<Map<String, Object>> getTrainerWorkload(
                    String trainerUsername, Integer year, Integer month,
                    String authorization, String transactionId) {

                Long totalDuration = trainerWorkloads.getOrDefault(trainerUsername, 0L);

                log.info("Mock WorkloadService called for trainer: {}, current workload: {}",
                        trainerUsername, totalDuration);

                Map<String, Object> response = new HashMap<>();
                response.put("username", trainerUsername);
                response.put("totalDuration", totalDuration);
                response.put("totalMinutes", totalDuration);

                // Years structure
                List<Map<String, Object>> years = new ArrayList<>();
                if (totalDuration > 0) {
                    Map<String, Object> yearData = new HashMap<>();
                    yearData.put("year", 2026);

                    List<Map<String, Object>> months = new ArrayList<>();
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", 5);
                    monthData.put("trainingsSummaryDuration", totalDuration);
                    monthData.put("totalDuration", totalDuration);
                    months.add(monthData);

                    yearData.put("months", months);
                    years.add(yearData);
                }
                response.put("years", years);

                return ResponseEntity.ok(response);
            }
        };
    }

    public static void addWorkload(String trainerUsername, long minutes) {
        trainerWorkloads.merge(trainerUsername, minutes, Long::sum);
        log.info("TEST: Manually added {} minutes to trainer {}. New total: {}",
                minutes, trainerUsername, trainerWorkloads.get(trainerUsername));
    }

    public static void removeWorkload(String trainerUsername, long minutes) {
        trainerWorkloads.compute(trainerUsername, (k, v) -> {
            if (v == null) return 0L;
            long result = v - minutes;
            return Math.max(0L, result);
        });
        log.info("TEST: Manually removed {} minutes from trainer {}. New total: {}",
                minutes, trainerUsername, trainerWorkloads.get(trainerUsername));
    }

    public static void resetWorkload(String trainerUsername) {
        trainerWorkloads.put(trainerUsername, 0L);
        log.info("TEST: Reset workload for trainer {}", trainerUsername);
    }

    public static long getWorkload(String trainerUsername) {
        return trainerWorkloads.getOrDefault(trainerUsername, 0L);
    }

    public static void clearAllWorkloads() {
        trainerWorkloads.clear();
        log.info("TEST: Cleared all workloads");
    }

    @Component
    @Profile("stg")
    @Slf4j
    public static class TestWorkloadMessageListener {

        @JmsListener(destination = "trainer.workload.queue")
        public void handleWorkloadMessage(TrainerWorkloadRequest request) {
            try {
                log.info("JMS: Received workload message: trainer={}, action={}, duration={}",
                        request.getTrainerUsername(), request.getActionType(), request.getTrainingDuration());

                String trainerUsername = request.getTrainerUsername();
                TrainerWorkloadRequest.ActionType actionType = request.getActionType();
                Integer duration = request.getTrainingDuration();

                if (trainerUsername != null && actionType != null && duration != null) {
                    switch (actionType) {
                        case ADD:
                            addWorkload(trainerUsername, duration.longValue());
                            break;
                        case DELETE:
                            removeWorkload(trainerUsername, duration.longValue());
                            break;
                        default:
                            log.warn("JMS: Unknown action type: {}", actionType);
                            break;
                    }
                } else {
                    log.warn("JMS: Invalid message - trainer: {}, action: {}, duration: {}",
                            trainerUsername, actionType, duration);
                }
            } catch (Exception e) {
                log.error("JMS: Error processing workload message: {}", request, e);
            }
        }
    }
}