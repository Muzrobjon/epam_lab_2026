package com.epam.gym.dto.request;

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

    @NotBlank(message = "Trainee password is required")
    private String traineePassword;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Trainer password is required")
    private String trainerPassword;

    @NotBlank(message = "Training name is required")
    private String trainingName;

    @NotNull(message = "Training date is required")
    private LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @Positive(message = "Training duration must be positive")
    private Integer trainingDuration;
}