package com.epam.gym.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTrainingRequest {

    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is required")
    private String trainingName;

    @NotNull(message = "Training date is required")
    @FutureOrPresent(message = "Training date must be today or in the future")  // ⭐ QO'SHILDI
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    @Max(value = 44640, message = "Training duration cannot exceed one month (44640 minutes)")
    private Integer trainingDuration;
}