package com.fithub.gym.controller;

import com.fithub.gym.entity.ClassType;
import com.fithub.gym.service.ClassTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/class-types")
public class ClassTypeController extends AbstractCrudController<ClassType> {
    public ClassTypeController(ClassTypeService service) {
        super(service);
    }
}
