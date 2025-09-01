package com.gcs.app.security;

import com.gcs.app.model.Role;
import com.gcs.app.model.User;
import com.gcs.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static com.gcs.app.model.enums.RoleType.ROLE_TRAINEE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {
        var username = "test.user";
        var user = createUser(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = service.loadUserByUsername(username);

        assertEquals(username, userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        assertEquals(1,  userDetails.getAuthorities().size());
        var authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_TRAINEE", authority.getAuthority());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        var username = "missing.user";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        var exception = assertThrows(
                UsernameNotFoundException.class, () -> service.loadUserByUsername(username));

        assertEquals("User not found: " + username, exception.getMessage());
    }

    private User createUser(String username) {
        var role = Role.builder()
                .id(1L)
                .roleType(ROLE_TRAINEE)
                .build();

        return User.builder()
                .username(username)
                .password("encoded-password")
                .isActive(true)
                .roles(Set.of(role))
                .build();
    }
}