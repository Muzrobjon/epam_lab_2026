package com.epam.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "Transaction ID for tracking", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String transactionId;

    @Schema(description = "Error message", example = "User not found")
    private String message;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Timestamp of the error", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Request path", example = "/api/trainees/John.Doe")
    private String path;
}