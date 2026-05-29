package com.epam.gym.client;

import com.epam.gym.context.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/trainers/workload")
@RequiredArgsConstructor
@Tag(name = "Trainer Workload", description = "Trainer workload operations")
@SecurityRequirement(name = "bearerAuth")
public class TrainerWorkloadController {

    private final WorkloadServiceClient workloadServiceClient;

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer workload summary")
    public ResponseEntity<Map<String, Object>> getTrainerWorkload(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest request) {

        String transactionId = getTransactionId(request);
        log.info("[TransactionId: {}] Getting workload for trainer: {}", transactionId, username);

        ResponseEntity<Map<String, Object>> response = workloadServiceClient
                .getTrainerWorkload(username, year, month, authorization, transactionId);

        log.info("[TransactionId: {}] Workload response received for trainer: {}",
                transactionId, username);

        return response;
    }

    private String getTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader("X-Transaction-Id");
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = TransactionContext.getTransactionId();
        }
        return transactionId != null ? transactionId : "UNKNOWN";
    }
}