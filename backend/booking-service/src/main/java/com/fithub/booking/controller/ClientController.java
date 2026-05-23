package com.fithub.booking.controller;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.dto.ClientProfileRequest;
import com.fithub.booking.entity.Client;
import com.fithub.booking.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController extends AbstractCrudController<Client> {
    private final ClientService service;
    private final JwtService jwtService;

    public ClientController(ClientService service, JwtService jwtService) {
        super(service);
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public Client me(@RequestHeader("Authorization") String authorization) {
        return service.findByAuthUserId(currentUserId(authorization));
    }

    @PostMapping("/me")
    public Client upsertMe(@RequestHeader("Authorization") String authorization, @Valid @RequestBody ClientProfileRequest request) {
        return service.upsertCurrentClient(currentUserId(authorization), request);
    }

    private Long currentUserId(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        return jwtService.userId(token);
    }
}
