package com.fithub.gym.service;

import com.fithub.gym.entity.Identifiable;
import com.fithub.gym.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public class CrudService<T extends Identifiable> {
    private final JpaRepository<T, Long> repository;
    private final String resourceName;

    protected CrudService(JpaRepository<T, Long> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public T findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(resourceName + " nu a fost gasit."));
    }

    public T create(T entity) {
        entity.setId(null);
        return repository.save(entity);
    }

    public T update(Long id, T entity) {
        findById(id);
        entity.setId(id);
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
