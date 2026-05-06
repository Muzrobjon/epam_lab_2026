package com.epam.gym.security;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return UserDetails when user exists in database")
    void loadUserByUsername_Success() {
        // GIVEN
        User user = new User();
        user.setId(1L);
        String USERNAME = "test_user";
        user.setUsername(USERNAME);
        user.setPassword("hashed_password");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setIsActive(true);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // WHEN
        UserDetails userDetails = userDetailsService.loadUserByUsername(USERNAME);

        // THEN
        assertNotNull(userDetails);
        assertEquals(USERNAME, userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername(USERNAME);

        // Verifying that UserDetails is specifically an instance of UserPrincipal
        assertTrue(userDetails instanceof UserPrincipal);
        UserPrincipal principal = (UserPrincipal) userDetails;
        assertEquals(1L, principal.getId());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // GIVEN
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // WHEN & THEN
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown_user"));

        assertTrue(exception.getMessage().contains("User not found: unknown_user"));
        verify(userRepository, times(1)).findByUsername("unknown_user");
    }
}