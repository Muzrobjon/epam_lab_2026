package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update trainer profile request")
public class UpdateTrainerRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "Alice.Smith", required = true)
    private String username;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Alice", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Smith", required = true)
    private String lastName;

    @NotNull(message = "IsActive status is required")
    @Schema(description = "Active status", example = "true", required = true)
    private Boolean isActive;
}