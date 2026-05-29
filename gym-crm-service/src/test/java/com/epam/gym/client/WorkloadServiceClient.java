package com.epam.gym.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(
        name = "trainer-workload-service",
        url = "${workload.service.url:http://localhost:8082}"
)
public interface WorkloadServiceClient {

    @GetMapping("/api/trainers/workload/{trainerUsername}")
    ResponseEntity<Map<String, Object>> getTrainerWorkload(
            @PathVariable("trainerUsername") String trainerUsername,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Transaction-Id") String transactionId
    );
}