package com.fithub.gym.service;

import com.fithub.gym.entity.Equipment;
import com.fithub.gym.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

@Service
public class EquipmentService extends CrudService<Equipment> {
    public EquipmentService(EquipmentRepository repository) {
        super(repository, "Echipamentul");
    }
}
