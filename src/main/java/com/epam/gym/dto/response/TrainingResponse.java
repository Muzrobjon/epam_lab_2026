package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Training information")
public class TrainingResponse {

    @Schema(description = "Training name", example = "Morning Yoga Session")
    private String trainingName;

    @Schema(description = "Training date", example = "2024-06-15")
    private LocalDate trainingDate;

    @Schema(description = "Training type", example = "YOGA")
    private TrainingTypeName trainingType;

    @Schema(description = "Training duration in minutes", example = "60")
    private Integer trainingDuration;

    @Schema(description = "Trainer name", example = "Alice Smith")
    private String trainerName;

    @Schema(description = "Trainee name", example = "John Doe")
    private String traineeName;
}