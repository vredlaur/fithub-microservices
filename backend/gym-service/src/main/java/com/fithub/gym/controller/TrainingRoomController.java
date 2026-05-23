package com.fithub.gym.controller;

import com.fithub.gym.entity.TrainingRoom;
import com.fithub.gym.service.TrainingRoomService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class TrainingRoomController extends AbstractCrudController<TrainingRoom> {
    public TrainingRoomController(TrainingRoomService service) {
        super(service);
    }
}
