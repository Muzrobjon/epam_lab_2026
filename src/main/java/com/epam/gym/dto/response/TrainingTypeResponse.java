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
@Schema(description = "Training type information")
public class TrainingTypeResponse {

    @Schema(description = "Training type ID", example = "1")
    private Long id;

    @Schema(description = "Training type name", example = "YOGA")
    private TrainingTypeName trainingTypeName;
}