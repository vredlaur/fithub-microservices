package com.fithub.gym.service;

import com.fithub.gym.entity.FitnessClass;
import com.fithub.gym.exception.InvalidOperationException;
import com.fithub.gym.repository.FitnessClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FitnessClassService extends CrudService<FitnessClass> {
    private static final Logger log = LoggerFactory.getLogger(FitnessClassService.class);
    private final FitnessClassRepository repository;

    public FitnessClassService(FitnessClassRepository repository) {
        super(repository, "Clasa fitness");
        this.repository = repository;
    }

    @Override
    public FitnessClass create(FitnessClass entity) {
        validateClass(entity);
        entity.setAvailableSlots(entity.getCapacity());
        log.info("Created fitness class {}", entity.getName());
        return super.create(entity);
    }

    @Override
    public FitnessClass update(Long id, FitnessClass entity) {
        validateClass(entity);
        entity.setAvailableSlots(Math.min(entity.getAvailableSlots(), entity.getCapacity()));
        log.info("Updated fitness class {}", id);
        return super.update(id, entity);
    }

    public boolean hasAvailability(Long id) {
        FitnessClass fitnessClass = findById(id);
        return "SCHEDULED".equals(fitnessClass.getStatus()) && fitnessClass.getAvailableSlots() > 0;
    }

    @Transactional
    public FitnessClass reserveSlot(Long id) {
        FitnessClass fitnessClass = findById(id);
        if (!hasAvailability(id)) {
            log.info("Reservation failed for class {}: no available slots", id);
            throw new InvalidOperationException("Nu mai sunt locuri disponibile pentru aceasta clasa.");
        }
        fitnessClass.setAvailableSlots(fitnessClass.getAvailableSlots() - 1);
        log.info("Reserved slot for fitness class {}", id);
        return repository.save(fitnessClass);
    }

    @Transactional
    public FitnessClass releaseSlot(Long id) {
        FitnessClass fitnessClass = findById(id);
        if (fitnessClass.getAvailableSlots() < fitnessClass.getCapacity()) {
            fitnessClass.setAvailableSlots(fitnessClass.getAvailableSlots() + 1);
        }
        log.info("Released slot for fitness class {}", id);
        return repository.save(fitnessClass);
    }

    private void validateClass(FitnessClass entity) {
        if (entity.getEndTime() != null && entity.getStartTime() != null && !entity.getEndTime().isAfter(entity.getStartTime())) {
            throw new InvalidOperationException("Ora de final trebuie sa fie dupa ora de inceput.");
        }
        if (entity.getAvailableSlots() > entity.getCapacity()) {
            entity.setAvailableSlots(entity.getCapacity());
        }
    }
}
