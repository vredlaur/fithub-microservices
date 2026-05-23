package com.fithub.gym.service;

import com.fithub.gym.entity.TrainingRoom;
import com.fithub.gym.repository.TrainingRoomRepository;
import org.springframework.stereotype.Service;

@Service
public class TrainingRoomService extends CrudService<TrainingRoom> {
    public TrainingRoomService(TrainingRoomRepository repository) {
        super(repository, "Sala");
    }
}
