package com.fithub.booking.controller;

import com.fithub.booking.entity.Client;
import com.fithub.booking.service.ClientService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController extends AbstractCrudController<Client> {
    public ClientController(ClientService service) {
        super(service);
    }
}
