package com.epam.gym.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current user info")
public class UserInfoResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private Collection<? extends GrantedAuthority> authorities;
}
