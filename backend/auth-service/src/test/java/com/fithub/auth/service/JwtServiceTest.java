package com.fithub.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(new ObjectMapper(), "test-secret-key", 60);

    @Test
    void generatedTokenContainsUserIdAndRoles() {
        User user = new User();
        user.setId(42L);
        user.setUsername("user");
        user.setEmail("user@fithub.local");
        user.setRoles(Set.of(new Role("USER")));

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.username(token)).isEqualTo("user");
        assertThat(jwtService.userId(token)).isEqualTo(42L);
        assertThat(jwtService.roles(token)).containsExactly("USER");
    }
}
