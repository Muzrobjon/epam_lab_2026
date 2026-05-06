package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainer profile response")
public class TrainerProfileResponse {

    @Schema(description = "Username", example = "Alice.Smith")
    private String username;

    @Schema(description = "First name", example = "Alice")
    private String firstName;

    @Schema(description = "Last name", example = "Smith")
    private String lastName;

    @Schema(description = "Specialization", example = "YOGA")
    private TrainingTypeName specialization;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "List of assigned trainees")
    private List<TraineeSummaryResponse> trainees;
}