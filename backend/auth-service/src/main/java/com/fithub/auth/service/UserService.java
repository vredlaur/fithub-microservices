package com.fithub.auth.service;

import com.fithub.auth.dto.UserRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.entity.User;
import com.fithub.auth.entity.UserProfile;
import com.fithub.auth.exception.DuplicateResourceException;
import com.fithub.auth.exception.ResourceNotFoundException;
import com.fithub.auth.repository.RoleRepository;
import com.fithub.auth.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost gasit."));
    }

    @Transactional
    public User create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username-ul este deja folosit.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Emailul este deja folosit.");
        }
        User user = new User();
        apply(user, request, true);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        User user = findById(id);
        apply(user, request, false);
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.delete(findById(id));
    }

    private void apply(User user, UserRequest request, boolean includePassword) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setEnabled(request.enabled());
        if (includePassword || request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        UserProfile profile = user.getProfile() == null ? new UserProfile() : user.getProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        user.setProfile(profile);

        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty()
            ? Set.of("USER")
            : request.roles();
        user.setRoles(requestedRoles.stream()
            .map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rolul " + name + " nu exista.")))
            .collect(Collectors.toSet()));
    }
}
