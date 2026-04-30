package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update trainee's trainers list request")
public class UpdateTraineeTrainersRequest {

    @NotBlank(message = "Trainee username is required")
    @Schema(description = "Trainee username",
            example = "John.Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String traineeUsername;

    @NotEmpty(message = "Trainers list cannot be empty")
    @Schema(description = "List of trainer usernames",
            example = "[\"Alice.Smith\", \"Bob.Johnson\"]",
            requiredMode = RequiredMode.REQUIRED)
    private List<String> trainerUsernames;
}