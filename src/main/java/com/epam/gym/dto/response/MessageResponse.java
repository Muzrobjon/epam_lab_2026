package com.epam.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard message response")
public class MessageResponse {

    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;

    @Builder.Default
    @Schema(description = "Timestamp", example = "2025-01-15T10:30:00")
    private String  timeStamp = LocalDateTime.now().toString();

    public MessageResponse(String message) {
        this.message = message;
        this.timeStamp = LocalDateTime.now().toString();
    }
}
