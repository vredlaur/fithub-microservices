package com.fithub.gym.service;

import com.fithub.gym.entity.ClassType;
import com.fithub.gym.repository.ClassTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ClassTypeService extends CrudService<ClassType> {
    public ClassTypeService(ClassTypeRepository repository) {
        super(repository, "Tipul de clasa");
    }
}
