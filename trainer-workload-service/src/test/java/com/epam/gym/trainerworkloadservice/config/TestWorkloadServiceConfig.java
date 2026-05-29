package com.epam.gym.trainerworkloadservice.config;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test configuration for mock workload tracking.
 * Bu class JMS message'larni qabul qilib, in-memory storage'da saqlaydi.
 */
@Slf4j
public class TestWorkloadServiceConfig {

    // ========== STATIC STORAGE ==========
    private static final Map<String, AtomicLong> workloadStorage = new ConcurrentHashMap<>();
    private static final Map<String, TrainerInfo> trainerInfoStorage = new ConcurrentHashMap<>();

    // ========== STATIC HELPER METHODS ==========

    /**
     * Trainer workload'ini olish
     */
    public static long getWorkload(String trainerUsername) {
        AtomicLong workload = workloadStorage.get(trainerUsername);
        long value = workload != null ? workload.get() : 0L;
        log.debug("📊 Getting workload for {}: {} minutes", trainerUsername, value);
        return value;
    }

    /**
     * Trainer workload'ini o'rnatish
     */
    public static void setWorkload(String trainerUsername, long minutes) {
        workloadStorage.put(trainerUsername, new AtomicLong(minutes));
        log.info("📝 Set workload for {}: {} minutes", trainerUsername, minutes);
    }

    /**
     * Workload qo'shish
     */
    public static void addWorkload(String trainerUsername, long minutes) {
        workloadStorage.computeIfAbsent(trainerUsername, k -> new AtomicLong(0))
                .addAndGet(minutes);
        log.debug("➕ Added {} minutes for {}, total: {}",
                minutes, trainerUsername, getWorkload(trainerUsername));
    }

    /**
     * Workload ayirish
     */
    public static void subtractWorkload(String trainerUsername, long minutes) {
        AtomicLong current = workloadStorage.get(trainerUsername);
        if (current != null) {
            long newValue = current.addAndGet(-minutes);
            if (newValue < 0) {
                current.set(0);
            }
            log.debug("➖ Subtracted {} minutes for {}, total: {}",
                    minutes, trainerUsername, current.get());
        }
    }

    /**
     * Trainer info olish
     */
    public static TrainerInfo getTrainerInfo(String trainerUsername) {
        return trainerInfoStorage.get(trainerUsername);
    }

    /**
     * Barcha workload'larni tozalash
     */
    public static void clearAll() {
        workloadStorage.clear();
        trainerInfoStorage.clear();
        log.info("🧹 All workloads cleared");
    }

    /**
     * Barcha workload'larni olish
     */
    public static Map<String, AtomicLong> getAllWorkloads() {
        return new ConcurrentHashMap<>(workloadStorage);
    }

    /**
     * Trainer sonini olish (getUserCount o'rniga)
     */
    public static int getTrainerCount() {
        return workloadStorage.size();
    }

    /**
     * Trainer sonini olish (alias)
     */
    public static int getUserCount() {
        return workloadStorage.size();
    }

    // ========== INTERNAL PROCESSING ==========

    /**
     * JMS message'ni process qilish (listener tomonidan chaqiriladi)
     */
    static void processWorkloadRequest(TrainerWorkloadRequest request, String transactionId) {
        String username = request.getTrainerUsername();

        log.info("🎭 Processing workload request for {} [txId: {}]", username, transactionId);

        // Trainer info saqlash
        trainerInfoStorage.put(username, new TrainerInfo(
                request.getTrainerFirstName(),
                request.getTrainerLastName(),
                request.getIsActive()
        ));

        // Workload yangilash
        workloadStorage.computeIfAbsent(username, k -> new AtomicLong(0));

        if (request.getActionType() == ActionType.ADD) {
            long newValue = workloadStorage.get(username).addAndGet(request.getTrainingDuration());
            log.info("🎭 ADD: {} minutes for {}. New total: {}",
                    request.getTrainingDuration(), username, newValue);
        } else if (request.getActionType() == ActionType.DELETE) {
            AtomicLong current = workloadStorage.get(username);
            long newValue = current.addAndGet(-request.getTrainingDuration());
            if (newValue < 0) {
                current.set(0);
                newValue = 0;
            }
            log.info("🎭 DELETE: {} minutes for {}. New total: {}",
                    request.getTrainingDuration(), username, newValue);
        }
    }

    // ========== TRAINER INFO RECORD ==========

    public record TrainerInfo(String firstName, String lastName, Boolean isActive) {}

    // ========== MOCK JMS LISTENER COMPONENT ==========

    /**
     * Mock JMS Listener - alohida component sifatida
     * Bu real listener'ni override qiladi test environment'da
     */
    @Component
    @Profile("stg")
    @Slf4j
    public static class MockWorkloadJmsListener {

        @PostConstruct
        public void init() {
            log.info("🎭 MockWorkloadJmsListener initialized for STG profile");
        }

        @JmsListener(
                destination = "${app.jms.queue.trainer-workload}",
                containerFactory = "jmsListenerContainerFactory"
        )
        public void onMessage(
                @Payload TrainerWorkloadRequest request,
                @Header(name = "X-Transaction-Id", required = false) String transactionId
        ) {
            String txId = transactionId != null ? transactionId : "unknown";

            log.info("🎭 MOCK LISTENER: Received message for {} [txId: {}]",
                    request.getTrainerUsername(), txId);

            try {
                // Static method orqali process qilish
                TestWorkloadServiceConfig.processWorkloadRequest(request, txId);

                log.info("🎭 MOCK LISTENER: Successfully processed message for {}",
                        request.getTrainerUsername());
            } catch (Exception e) {
                log.error("🎭 MOCK LISTENER: Error processing message: {}", e.getMessage(), e);
            }
        }
    }
}