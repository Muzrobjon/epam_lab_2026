package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update trainee profile request")
public class UpdateTraineeRequest {

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

    @NotBlank(message = "First name is required")
    @Schema(description = "First name",
            example = "John",
            requiredMode = RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name",
            example = "Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth",
            example = "1995-03-20",
            requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate dateOfBirth;

    @Schema(description = "Address",
            example = "456 Oak Ave, Boston",
            requiredMode = RequiredMode.NOT_REQUIRED)
    private String address;

    @NotNull(message = "IsActive status is required")
    @Schema(description = "Active status",
            example = "true",
            requiredMode = RequiredMode.REQUIRED)
    private Boolean isActive;
}