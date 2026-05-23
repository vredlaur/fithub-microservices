package com.fithub.gym.service;

import com.fithub.gym.entity.Location;
import com.fithub.gym.repository.LocationRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationService extends CrudService<Location> {
    public LocationService(LocationRepository repository) {
        super(repository, "Locatia");
    }
}
