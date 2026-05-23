package com.fithub.booking.controller;

import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.service.ClientSubscriptionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class ClientSubscriptionController extends AbstractCrudController<ClientSubscription> {
    public ClientSubscriptionController(ClientSubscriptionService service) {
        super(service);
    }
}
