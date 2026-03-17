package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Schema(description = "Add training request")
public class AddTrainingRequest {

    @NotBlank(message = "Trainee username is required")
    @Schema(description = "Trainee username",
            example = "John.Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String traineeUsername;

    @NotBlank(message = "Trainee password is required")
    @Schema(description = "Trainee password",
            example = "traineePass123",
            requiredMode = RequiredMode.REQUIRED)
    private String traineePassword;

    @NotBlank(message = "Trainer username is required")
    @Schema(description = "Trainer username",
            example = "Alice.Smith",
            requiredMode = RequiredMode.REQUIRED)
    private String trainerUsername;

    @NotBlank(message = "Trainer password is required")
    @Schema(description = "Trainer password",
            example = "trainerPass123",
            requiredMode = RequiredMode.REQUIRED)
    private String trainerPassword;

    @NotBlank(message = "Training name is required")
    @Schema(description = "Training name",
            example = "Morning Yoga Session",
            requiredMode = RequiredMode.REQUIRED)
    private String trainingName;

    @NotNull(message = "Training date is required")
    @Schema(description = "Training date",
            example = "2024-06-15",
            requiredMode = RequiredMode.REQUIRED)
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Schema(description = "Training duration in minutes",
            example = "60",
            requiredMode = RequiredMode.REQUIRED)
    private Integer trainingDuration;
}