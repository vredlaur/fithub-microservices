package com.fithub.gym.service;

import com.fithub.gym.entity.Trainer;
import com.fithub.gym.repository.TrainerRepository;
import org.springframework.stereotype.Service;

@Service
public class TrainerService extends CrudService<Trainer> {
    public TrainerService(TrainerRepository repository) {
        super(repository, "Antrenorul");
    }
}
