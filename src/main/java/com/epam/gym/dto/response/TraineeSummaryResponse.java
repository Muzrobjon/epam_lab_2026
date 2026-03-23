package com.epam.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainee summary information")
public class TraineeSummaryResponse {

    @Schema(description = "Trainee username", example = "John.Doe")
    private String username;

    @Schema(description = "Trainee first name", example = "John")
    private String firstName;

    @Schema(description = "Trainee last name", example = "Doe")
    private String lastName;
}