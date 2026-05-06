package com.epam.gym.dto.request;

import com.epam.gym.enums.TrainingTypeName;
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
@Schema(description = "Trainer registration request")
public class TrainerRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "First name of the trainer",
            example = "Alice",
            requiredMode = RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name of the trainer",
            example = "Smith",
            requiredMode = RequiredMode.REQUIRED)
    private String lastName;

    @NotNull(message = "Specialization is required")
    @Schema(description = "Training specialization",
            example = "YOGA",
            requiredMode = RequiredMode.REQUIRED)
    private TrainingTypeName specialization;
}