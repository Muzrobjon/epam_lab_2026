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
@Schema(description = "Registration response with credentials")
public class RegistrationResponse {

    @Schema(description = "Generated username", example = "John.Doe")
    private String username;

    @Schema(description = "Generated password", example = "aB3$xY9@kL")
    private String password;
}