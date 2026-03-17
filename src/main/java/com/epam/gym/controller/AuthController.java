package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.response.ErrorResponse;
import com.epam.gym.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final GymFacade gymFacade;

    @Operation(summary = "User login", description = "Authenticate user with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    // TODO:
    //  DANGER!!! With GET you are exposing credentials in URL and making it visible for all
    //  possible middlewares between client and server.
    //  Also note that GET is idempotent meaning there is no side effects and state changes on the server, while
    //  during login most real-world system (and we later in the course) will create some sort of auth state
    @GetMapping("/login")
    public ResponseEntity<Void> login(@Valid @ModelAttribute LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Try trainee first, then trainer
        try {
            gymFacade.authenticateTrainee(request.getUsername(), request.getPassword());
        } catch (Exception e) {
            // TODO:
            //  “Trainee or trainer” is normal business logic, not exceptional control flow and there is a ton of
            //  problems with exception-driven business branching. Without going into details let's keep
            //  responsibilities clear and separate:
            //  User is our profile representation: to work with profile related data the system does not need to know
            //  whether it's a trainer or trainee.
            //  You will need to refactor AbstractUserService to just UserService:)
            gymFacade.authenticateTrainer(request.getUsername(), request.getPassword());
        }

        log.info("User {} logged in successfully", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.getUsername());

        // Try trainee first, then trainer
        try {
            gymFacade.changeTraineePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
        } catch (Exception e) {
            gymFacade.changeTrainerPassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
        }

        log.info("Password changed successfully for user: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }
}