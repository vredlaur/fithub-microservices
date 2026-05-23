package com.fithub.gym.controller;

import com.fithub.gym.entity.Trainer;
import com.fithub.gym.service.TrainerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController extends AbstractCrudController<Trainer> {
    public TrainerController(TrainerService service) {
        super(service);
    }
}
