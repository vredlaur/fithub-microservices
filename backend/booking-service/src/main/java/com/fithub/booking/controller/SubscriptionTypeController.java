package com.fithub.booking.controller;

import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.service.SubscriptionTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription-types")
public class SubscriptionTypeController extends AbstractCrudController<SubscriptionType> {
    public SubscriptionTypeController(SubscriptionTypeService service) {
        super(service);
    }
}
