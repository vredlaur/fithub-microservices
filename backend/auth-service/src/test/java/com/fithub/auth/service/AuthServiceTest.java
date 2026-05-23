package com.fithub.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fithub.auth.dto.RegisterRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import com.fithub.auth.repository.RoleRepository;
import com.fithub.auth.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @InjectMocks
    AuthService authService;

    @Test
    void registerCreatesUserWithDefaultRoleAndToken() {
        Role role = new Role("USER");
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(userRepository.existsByEmail("ana@fithub.local")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt");

        var response = authService.register(new RegisterRequest(
            "ana",
            "ana@fithub.local",
            "Password1",
            "Ana",
            "Pop",
            "0700000000"
        ));

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.token()).isEqualTo("jwt");
        assertThat(response.roles()).containsExactly("USER");
    }
}
