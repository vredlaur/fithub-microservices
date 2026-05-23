package com.fithub.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.auth.dto.UserRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import com.fithub.auth.entity.UserProfile;
import com.fithub.auth.exception.DuplicateResourceException;
import com.fithub.auth.exception.ResourceNotFoundException;
import com.fithub.auth.repository.RoleRepository;
import com.fithub.auth.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void createStoresEncodedPasswordProfileAndRoles() {
        Role admin = new Role("ADMIN");
        when(userRepository.existsByUsername("admin2")).thenReturn(false);
        when(userRepository.existsByEmail("admin2@fithub.local")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);
        User saved = service.create(request("admin2", "admin2@fithub.local", "Password1", true, Set.of("ADMIN")));

        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.getProfile().getFirstName()).isEqualTo("Ana");
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly("ADMIN");
    }

    @Test
    void createRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("ana")).thenReturn(true);
        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);

        assertThatThrownBy(() -> service.create(request("ana", "ana@fithub.local", "Password1", true, Set.of("USER"))))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateKeepsPasswordWhenBlankAndUpdatesProfile() {
        Role userRole = new Role("USER");
        User user = new User();
        user.setId(1L);
        user.setPassword("old");
        user.setProfile(new UserProfile());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(user)).thenReturn(user);

        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);
        User updated = service.update(1L, request("ana", "ana@fithub.local", "", true, Set.of("USER")));

        assertThat(updated.getPassword()).isEqualTo("old");
        assertThat(updated.getProfile().getLastName()).isEqualTo("Pop");
    }

    @Test
    void findAllDelegatesToRepository() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(java.util.List.of(new User()));
        when(userRepository.findAll(pageRequest)).thenReturn(page);

        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);

        assertThat(service.findAll(pageRequest)).isSameAs(page);
    }

    @Test
    void deleteRemovesExistingUser() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);
        service.delete(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void missingRoleThrowsNotFound() {
        when(userRepository.existsByUsername("ana")).thenReturn(false);
        when(userRepository.existsByEmail("ana@fithub.local")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        UserService service = new UserService(userRepository, roleRepository, passwordEncoder);

        assertThatThrownBy(() -> service.create(request("ana", "ana@fithub.local", "Password1", true, Set.of("ADMIN"))))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private UserRequest request(String username, String email, String password, boolean enabled, Set<String> roles) {
        return new UserRequest(username, email, password, enabled, "Ana", "Pop", "0700000000", roles);
    }
}
