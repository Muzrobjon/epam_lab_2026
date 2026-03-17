package com.epam.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainee profile response")
public class TraineeProfileResponse {

    @Schema(description = "Username", example = "John.Doe")
    private String username;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Date of birth", example = "1995-03-20")
    private LocalDate dateOfBirth;

    @Schema(description = "Address", example = "123 Main St, New York")
    private String address;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "List of assigned trainers")
    private List<TrainerSummaryResponse> trainers;
}