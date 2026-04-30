package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Set active status request (idempotent)")
public class SetActiveRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username",
            example = "John.Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password for authentication",
            example = "password123",
            requiredMode = RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "IsActive status is required")
    @Schema(description = "New active status",
            example = "false",
            requiredMode = RequiredMode.REQUIRED)
    private Boolean isActive;
}