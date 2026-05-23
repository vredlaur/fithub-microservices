package com.fithub.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.auth.dto.RoleRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.exception.DuplicateResourceException;
import com.fithub.auth.exception.ResourceNotFoundException;
import com.fithub.auth.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
    @Mock
    RoleRepository repository;

    @Test
    void findAllReturnsRepositoryRoles() {
        Role role = new Role("USER");
        when(repository.findAll()).thenReturn(List.of(role));

        RoleService service = new RoleService(repository);

        assertThat(service.findAll()).containsExactly(role);
    }

    @Test
    void createRejectsDuplicateRoleName() {
        when(repository.existsByName("ADMIN")).thenReturn(true);
        RoleService service = new RoleService(repository);

        assertThatThrownBy(() -> service.create(new RoleRequest("ADMIN")))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createStoresNewRole() {
        Role saved = new Role("TRAINER");
        saved.setId(3L);
        when(repository.existsByName("TRAINER")).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any(Role.class))).thenReturn(saved);

        RoleService service = new RoleService(repository);

        assertThat(service.create(new RoleRequest("TRAINER")).getId()).isEqualTo(3L);
    }

    @Test
    void updateChangesRoleName() {
        Role role = new Role("USER");
        when(repository.findById(1L)).thenReturn(Optional.of(role));
        when(repository.save(role)).thenReturn(role);

        RoleService service = new RoleService(repository);

        assertThat(service.update(1L, new RoleRequest("ADMIN")).getName()).isEqualTo("ADMIN");
    }

    @Test
    void deleteRemovesExistingRole() {
        Role role = new Role("USER");
        when(repository.findById(1L)).thenReturn(Optional.of(role));

        RoleService service = new RoleService(repository);
        service.delete(1L);

        verify(repository).delete(role);
    }

    @Test
    void updateMissingRoleThrowsNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        RoleService service = new RoleService(repository);

        assertThatThrownBy(() -> service.update(9L, new RoleRequest("ADMIN")))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
