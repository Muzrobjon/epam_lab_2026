package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainer summary information")
public class TrainerSummaryResponse {

    @Schema(description = "Trainer username", example = "Alice.Smith")
    private String username;

    @Schema(description = "Trainer first name", example = "Alice")
    private String firstName;

    @Schema(description = "Trainer last name", example = "Smith")
    private String lastName;

    @Schema(description = "Trainer specialization", example = "YOGA")
    private TrainingTypeName specialization;
}