package com.epam.gym.trainerworkloadservice.listener;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import jakarta.jms.Message;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageListener {

    private final TrainerWorkloadService workloadService;
    private final Validator validator;
    private final JmsTemplate jmsTemplate;

    private static final String DLQ = "trainer.workload.dlq";

    @JmsListener(
            destination = "${app.jms.queue.trainer-workload}",
            containerFactory = "jmsListenerContainerFactory"
    )
    public void onMessage(@Payload TrainerWorkloadRequest request,
                          @Header(name = "X-Transaction-Id", required = false) String transactionId,
                          Message rawMessage) {

        if (transactionId == null) transactionId = "UNKNOWN";

        log.info("[TransactionId: {}] Received workload message: trainer={}, action={}",
                transactionId, request.getTrainerUsername(), request.getActionType());

        Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.error("[TransactionId: {}] Invalid message, sending to DLQ: {}", transactionId, errors);
            sendToDlq(request, transactionId, "Validation failed: " + errors);
            return;
        }

        try {
            workloadService.processWorkload(request, transactionId);
            log.info("[TransactionId: {}] Workload processed successfully", transactionId);
        } catch (Exception e) {
            log.error("[TransactionId: {}] Processing failed: {}", transactionId, e.getMessage(), e);
            throw new RuntimeException("Failed to process workload", e);
        }
    }

    private void sendToDlq(TrainerWorkloadRequest request, String transactionId, String reason) {
        try {
            final String txId = transactionId;
            jmsTemplate.convertAndSend(DLQ, request, msg -> {
                msg.setStringProperty("X-Transaction-Id", txId);
                msg.setStringProperty("X-DLQ-Reason", reason);
                return msg;
            });
            log.warn("[TransactionId: {}] Message routed to DLQ '{}'", transactionId, DLQ);
        } catch (Exception ex) {
            log.error("[TransactionId: {}] Failed to send to DLQ: {}", transactionId, ex.getMessage(), ex);
        }
    }
}