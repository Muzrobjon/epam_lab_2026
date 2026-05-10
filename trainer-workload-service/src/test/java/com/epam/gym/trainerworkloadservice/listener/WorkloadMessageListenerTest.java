package com.epam.gym.trainerworkloadservice.listener;

import com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest;
import com.epam.gym.trainerworkloadservice.enums.ActionType;
import com.epam.gym.trainerworkloadservice.listener.WorkloadMessageListener.MessageProcessingException;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import jakarta.validation.Path;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    @Mock
    private Validator validator;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private WorkloadMessageListener listener;

    @Captor
    private ArgumentCaptor<String> transactionIdCaptor;

    private TrainerWorkloadRequest validRequest;

    @BeforeEach
    void setUp() throws Exception {
        // Set DLQ destination via reflection
        Field dlqField = WorkloadMessageListener.class.getDeclaredField("dlqDestination");
        dlqField.setAccessible(true);
        dlqField.set(listener, "trainer.workload.dlq");

        validRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("John.Doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 5, 11))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();
    }

    @Nested
    class SuccessfulProcessing {

        @Test
        void shouldProcessValidMessage_WithProvidedTransactionId() {
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Collections.emptySet());

            listener.onMessage(validRequest, "txn-123");

            verify(workloadService).processWorkload(eq(validRequest), eq("txn-123"));
        }

        @Test
        void shouldGenerateTransactionId_WhenNull() {
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Collections.emptySet());

            listener.onMessage(validRequest, null);

            verify(workloadService).processWorkload(eq(validRequest), transactionIdCaptor.capture());
            assertThat(transactionIdCaptor.getValue()).startsWith("gen-");
        }

        @Test
        void shouldGenerateTransactionId_WhenBlank() {
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Collections.emptySet());

            listener.onMessage(validRequest, "   ");

            verify(workloadService).processWorkload(eq(validRequest), transactionIdCaptor.capture());
            assertThat(transactionIdCaptor.getValue()).startsWith("gen-");
        }

        @Test
        void shouldGenerateTransactionId_WhenEmpty() {
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Collections.emptySet());

            listener.onMessage(validRequest, "");

            verify(workloadService).processWorkload(eq(validRequest), transactionIdCaptor.capture());
            assertThat(transactionIdCaptor.getValue()).startsWith("gen-");
        }
    }

    @Nested
    class ValidationFailure {

        @Test
        void shouldSendToDlq_WhenValidationFails() {
            ConstraintViolation<TrainerWorkloadRequest> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("trainerUsername");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("is required");
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Set.of(violation));

            listener.onMessage(validRequest, "txn-456");

            verify(workloadService, never()).processWorkload(any(), any());
            verify(jmsTemplate).convertAndSend(
                    eq("trainer.workload.dlq"),
                    eq(validRequest),
                    any(MessagePostProcessor.class));
        }

        @Test
        void shouldNotThrowException_WhenDlqSendFails() {
            ConstraintViolation<TrainerWorkloadRequest> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("field");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("error");
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Set.of(violation));

            doThrow(new RuntimeException("DLQ unavailable"))
                    .when(jmsTemplate).convertAndSend(anyString(), any(Object.class), any(MessagePostProcessor.class));

            // Should not throw — DLQ failure is caught
            listener.onMessage(validRequest, "txn-789");

            verify(workloadService, never()).processWorkload(any(), any());
        }
    }

    @Nested
    class ProcessingFailure {

        @Test
        void shouldThrowMessageProcessingException_WhenServiceFails() {
            when(validator.validate(any(TrainerWorkloadRequest.class))).thenReturn(Collections.emptySet());
            doThrow(new RuntimeException("DB error"))
                    .when(workloadService).processWorkload(any(), any());

            assertThatThrownBy(() -> listener.onMessage(validRequest, "txn-error"))
                    .isInstanceOf(MessageProcessingException.class)
                    .hasMessageContaining("txn-error")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class MessageProcessingExceptionTest {

        @Test
        void shouldCreateExceptionWithMessageAndCause() {
            RuntimeException cause = new RuntimeException("root cause");
            MessageProcessingException exception =
                    new MessageProcessingException("test message", cause);

            assertThat(exception.getMessage()).isEqualTo("test message");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }
}