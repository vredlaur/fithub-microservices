package com.fithub.auth.service;

import com.fithub.auth.dto.AuthResponse;
import com.fithub.auth.dto.LoginRequest;
import com.fithub.auth.dto.RegisterRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import com.fithub.auth.entity.UserProfile;
import com.fithub.auth.exception.DuplicateResourceException;
import com.fithub.auth.exception.ResourceNotFoundException;
import com.fithub.auth.repository.RoleRepository;
import com.fithub.auth.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username-ul este deja folosit.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Emailul este deja folosit.");
        }
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new ResourceNotFoundException("Rolul USER nu exista."));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        user.setProfile(profile);

        User saved = userRepository.save(user);
        log.info("Created user {}", saved.getUsername());
        return response(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost gasit."));
        log.info("Successful login for {}", request.username());
        return response(user);
    }

    public AuthResponse me(String username) {
        return response(userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost gasit.")));
    }

    private AuthResponse response(User user) {
        return new AuthResponse(
            jwtService.generateToken(user),
            user.getUsername(),
            user.getEmail(),
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
