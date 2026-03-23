package com.epam.gym.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trainee registration request")
public class TraineeRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "First name of the trainee",
            example = "John",
            requiredMode = RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name of the trainee",
            example = "Doe",
            requiredMode = RequiredMode.REQUIRED)
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth",
            example = "1995-03-20",
            requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate dateOfBirth;

    @Schema(description = "Address",
            example = "123 Main St, New York",
            requiredMode = RequiredMode.NOT_REQUIRED)
    private String address;
}