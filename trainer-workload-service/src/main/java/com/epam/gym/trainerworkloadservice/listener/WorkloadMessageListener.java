package com.epam.gym.trainerworkloadservice.listener;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private static final String TRANSACTION_ID_KEY = "X-Transaction-Id";

    private final TrainerWorkloadService workloadService;
    private final Validator validator;
    private final JmsTemplate jmsTemplate;

    @Value("${app.jms.queue.trainer-workload-dlq}")
    private String dlqDestination;

    @JmsListener(
            destination = "${app.jms.queue.trainer-workload}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void onMessage(@Payload TrainerWorkloadRequest request,
                          @Header(name = TRANSACTION_ID_KEY, required = false) String transactionId) {

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "gen-" + UUID.randomUUID();
        }
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        try {
            log.info("[TransactionId: {}] Received workload message: trainer={}, action={}",
                    transactionId, request.getTrainerUsername(), request.getActionType());

            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("; "));

                log.error("[TransactionId: {}] Invalid message, routing to DLQ: {}", transactionId, errors);
                sendToDlq(request, transactionId, "Validation failed: " + errors);
                return;
            }

            workloadService.processWorkload(request, transactionId);

        } catch (Exception e) {
            log.error("[TransactionId: {}] Message processing failed: {}", transactionId, e.getMessage(), e);
            throw new MessageProcessingException(
                    "Failed to process workload for transaction: " + transactionId, e);
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private void sendToDlq(TrainerWorkloadRequest request, String transactionId, String reason) {
        try {
            jmsTemplate.convertAndSend(dlqDestination, request, msg -> {
                msg.setStringProperty(TRANSACTION_ID_KEY, transactionId);
                msg.setStringProperty("X-DLQ-Reason", reason);
                msg.setStringProperty("X-DLQ-Timestamp", String.valueOf(System.currentTimeMillis()));
                return msg;
            });
            log.warn("[TransactionId: {}] Message routed to DLQ '{}'", transactionId, dlqDestination);
        } catch (Exception ex) {
            log.error("[TransactionId: {}] Failed to send to DLQ: {}", transactionId, ex.getMessage(), ex);
        }
    }

    public static class MessageProcessingException extends RuntimeException {
        public MessageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}