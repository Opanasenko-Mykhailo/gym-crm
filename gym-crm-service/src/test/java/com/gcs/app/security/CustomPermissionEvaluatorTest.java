package com.gcs.app.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPermissionEvaluatorTest {

    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator();

    @Test
    @DisplayName("Given same username and matching role, when hasPermission called, then return true")
    void givenSameUserAndRole_whenHasPermission_thenReturnTrue() {
        var username = "john.doe";
        var role = "TRAINEE";
        var auth = new UsernamePasswordAuthenticationToken(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));

        boolean actual = evaluator.hasPermission(auth, username, role);

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Given different username, when hasPermission called, then return false")
    void givenDifferentUser_whenHasPermission_thenReturnFalse() {
        var auth = new UsernamePasswordAuthenticationToken("john.doe", "password",
                List.of(new SimpleGrantedAuthority("ROLE_TRAINEE")));

        boolean actual = evaluator.hasPermission(auth, "jane.doe", "TRAINEE");

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("Given same username but missing role, when hasPermission called, then return false")
    void givenSameUserButMissingRole_whenHasPermission_thenReturnFalse() {
        var username = "john.doe";
        var auth = new UsernamePasswordAuthenticationToken(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_TRAINER")));

        boolean actual = evaluator.hasPermission(auth, username, "TRAINEE");

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("Given unauthenticated auth, when hasPermission called, then return false")
    void givenUnauthenticated_whenHasPermission_thenReturnFalse() {
        var username = "john.doe";
        var auth = new UsernamePasswordAuthenticationToken(username, "password");
        auth.setAuthenticated(false);

        boolean actual = evaluator.hasPermission(auth, username, "TRAINEE");

        assertThat(actual).isFalse();
    }
}