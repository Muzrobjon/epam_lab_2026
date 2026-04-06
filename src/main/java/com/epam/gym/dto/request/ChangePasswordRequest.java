package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "John.Doe")
    private String username;

    @NotBlank(message = "Old password is required")
    @Schema(description = "Current password", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    @Schema(description = "New password", example = "newPassword123")
    private String newPassword;
}