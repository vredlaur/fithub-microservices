package com.fithub.gym.controller;

import com.fithub.gym.entity.Equipment;
import com.fithub.gym.service.EquipmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController extends AbstractCrudController<Equipment> {
    public EquipmentController(EquipmentService service) {
        super(service);
    }
}
