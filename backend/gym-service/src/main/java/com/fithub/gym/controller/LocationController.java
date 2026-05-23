package com.fithub.gym.controller;

import com.fithub.gym.entity.Location;
import com.fithub.gym.service.LocationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController extends AbstractCrudController<Location> {
    public LocationController(LocationService service) {
        super(service);
    }
}
