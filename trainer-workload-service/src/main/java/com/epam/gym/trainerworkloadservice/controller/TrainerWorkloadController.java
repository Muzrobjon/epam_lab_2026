package com.epam.gym.trainerworkloadservice.controller;

import com.epam.gym.trainerworkloadservice.dto.response.TrainerWorkloadResponse;
import com.epam.gym.trainerworkloadservice.service.TrainerWorkloadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/trainer-workload")
@RequiredArgsConstructor
public class TrainerWorkloadController {

    private final TrainerWorkloadService workloadService;



    @GetMapping("/{trainerUsername:.+}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
            @PathVariable String trainerUsername,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest httpRequest) {

        String transactionId = getTransactionId(httpRequest);

        log.info("[TransactionId: {}] Received get workload request for trainer: {}, year: {}, month: {}",
                transactionId, trainerUsername, year, month);

        TrainerWorkloadResponse response = workloadService
                .getTrainerWorkload(trainerUsername, year, month, transactionId);

        log.info("[TransactionId: {}] Returning workload for trainer: {} - Status: 200 OK",
                transactionId, trainerUsername);

        return ResponseEntity.ok(response);
    }
    private String getTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader("X-Transaction-Id");
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = (String) request.getAttribute("transactionId");
        }
        return transactionId != null ? transactionId : "UNKNOWN";
    }
}