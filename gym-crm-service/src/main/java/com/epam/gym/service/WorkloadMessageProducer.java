package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerWorkloadRequest;
import com.epam.gym.entity.Trainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;

    @Value("${app.jms.queue.trainer-workload}")
    private String workloadQueue;

    public void sendNotification(Trainer trainer, LocalDate trainingDate,
                                 Integer duration, TrainerWorkloadRequest.ActionType actionType) {
        String transactionId = getTransactionId();

        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername(trainer.getUser().getUsername())
                .trainerFirstName(trainer.getUser().getFirstName())
                .trainerLastName(trainer.getUser().getLastName())
                .isActive(trainer.getUser().getIsActive())
                .trainingDate(trainingDate)
                .trainingDuration(duration)
                .actionType(actionType)
                .build();

        try {
            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                message.setStringProperty("X-Transaction-Id", transactionId);
                return message;
            });

            log.info("[TransactionId: {}] Workload message sent to queue '{}': trainer={}, action={}",
                    transactionId, workloadQueue, request.getTrainerUsername(), actionType);

        } catch (Exception e) {
             log.error("[TransactionId: {}] Failed to send workload message: {}",
                    transactionId, e.getMessage(), e);
        }
    }

    private String getTransactionId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String tid = attributes.getRequest().getHeader("X-Transaction-Id");
            if (tid == null) {
                tid = (String) attributes.getRequest().getAttribute("transactionId");
            }
            return tid != null ? tid : "UNKNOWN";
        }
        return "UNKNOWN";
    }
}