package com.epam.gym.config;

import com.epam.gym.client.WorkloadServiceClient;
import com.epam.gym.dto.request.TrainerWorkloadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
@Profile("stg")
@EnableJms
@Slf4j
public class TestWorkloadServiceConfig {

    private static final Map<String, Long> trainerWorkloads = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("vm://localhost?broker.persistent=false");
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    @Primary
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Bean
    @Primary
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory,
                                   MessageConverter messageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    @Primary
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency("1-1");
        factory.setSessionTransacted(false);
        return factory;
    }

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

                List<Map<String, Object>> years = new ArrayList<>();
                if (totalDuration > 0) {
                    Map<String, Object> yearData = new HashMap<>();
                    yearData.put("year", year != null ? year : 2026);

                    List<Map<String, Object>> months = new ArrayList<>();
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", month != null ? month : 5);
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
        log.info("TEST: Added {} minutes to trainer {}. New total: {}",
                minutes, trainerUsername, trainerWorkloads.get(trainerUsername));
    }

    public static void removeWorkload(String trainerUsername, long minutes) {
        trainerWorkloads.compute(trainerUsername, (k, v) -> {
            if (v == null) return 0L;
            return Math.max(0L, v - minutes);
        });
        log.info("TEST: Removed {} minutes from trainer {}. New total: {}",
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
                        request.getTrainerUsername(),
                        request.getActionType(),
                        request.getTrainingDuration());

                String trainerUsername = request.getTrainerUsername();
                TrainerWorkloadRequest.ActionType actionType = request.getActionType();
                Integer duration = request.getTrainingDuration();

                if (trainerUsername == null || trainerUsername.trim().isEmpty()) {
                    log.warn("JMS: Invalid message - trainerUsername is null or empty");
                    return;
                }

                if (actionType == null) {
                    log.warn("JMS: Invalid message - actionType is null");
                    return;
                }

                if (duration == null || duration < 0) {
                    log.warn("JMS: Invalid message - duration is null or negative: {}", duration);
                    return;
                }

                switch (actionType) {
                    case ADD:
                        addWorkload(trainerUsername, duration.longValue());
                        log.info("JMS: Successfully added {} minutes to trainer {}",
                                duration, trainerUsername);
                        break;

                    case DELETE:
                        removeWorkload(trainerUsername, duration.longValue());
                        log.info("JMS: Successfully removed {} minutes from trainer {}",
                                duration, trainerUsername);
                        break;

                    default:
                        log.warn("JMS: Unknown action type: {}", actionType);
                        break;
                }

            } catch (Exception e) {
                log.error("JMS: Error processing workload message: {}", request, e);
            }
        }
    }
}