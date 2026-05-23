package com.fithub.gym.controller;

import com.fithub.gym.entity.FitnessClass;
import com.fithub.gym.service.FitnessClassService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classes")
public class FitnessClassController extends AbstractCrudController<FitnessClass> {
    private final FitnessClassService service;

    public FitnessClassController(FitnessClassService service) {
        super(service);
        this.service = service;
    }

    @GetMapping("/{id}/availability")
    public Map<String, Object> availability(@PathVariable Long id) {
        FitnessClass fitnessClass = service.findById(id);
        return Map.of(
            "fitnessClassId", id,
            "available", service.hasAvailability(id),
            "availableSlots", fitnessClass.getAvailableSlots(),
            "capacity", fitnessClass.getCapacity()
        );
    }

    @PostMapping("/{id}/reserve-slot")
    public FitnessClass reserveSlot(@PathVariable Long id) {
        return service.reserveSlot(id);
    }

    @PostMapping("/{id}/release-slot")
    public FitnessClass releaseSlot(@PathVariable Long id) {
        return service.releaseSlot(id);
    }
}
