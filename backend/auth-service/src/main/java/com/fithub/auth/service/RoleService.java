package com.fithub.auth.service;

import com.fithub.auth.dto.RoleRequest;
import com.fithub.auth.entity.Role;
import com.fithub.auth.exception.DuplicateResourceException;
import com.fithub.auth.exception.ResourceNotFoundException;
import com.fithub.auth.repository.RoleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role create(RoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Rolul exista deja.");
        }
        return roleRepository.save(new Role(request.name()));
    }

    public Role update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rolul nu a fost gasit."));
        role.setName(request.name());
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rolul nu a fost gasit."));
        roleRepository.delete(role);
    }
}
