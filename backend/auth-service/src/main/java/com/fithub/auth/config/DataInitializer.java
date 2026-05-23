package com.fithub.auth.config;

import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import com.fithub.auth.entity.UserProfile;
import com.fithub.auth.repository.RoleRepository;
import com.fithub.auth.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedAuthData(
        RoleRepository roleRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER")));

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(user("admin", "admin@fithub.local", "Admin123!", "Admin", "FitHub", Set.of(adminRole, userRole), passwordEncoder));
            }
            if (!userRepository.existsByUsername("user")) {
                userRepository.save(user("user", "user@fithub.local", "User123!", "User", "FitHub", Set.of(userRole), passwordEncoder));
            }
        };
    }

    private User user(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        Set<Role> roles,
        PasswordEncoder passwordEncoder
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(roles);
        UserProfile profile = new UserProfile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone("0700000000");
        user.setProfile(profile);
        return user;
    }
}
