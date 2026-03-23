package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username",
            example = "John.Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Old password is required")
    @Schema(description = "Current password",
            example = "oldPassword123",
            requiredMode = RequiredMode.REQUIRED)
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password",
            example = "newPassword123",
            requiredMode = RequiredMode.REQUIRED)
    private String newPassword;
}