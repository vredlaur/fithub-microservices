package com.fithub.booking.controller;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.dto.PurchaseSubscriptionRequest;
import com.fithub.booking.dto.PurchaseSubscriptionResponse;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.service.ClientSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class ClientSubscriptionController extends AbstractCrudController<ClientSubscription> {
    private final ClientSubscriptionService service;
    private final JwtService jwtService;

    public ClientSubscriptionController(ClientSubscriptionService service, JwtService jwtService) {
        super(service);
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public Page<ClientSubscription> mine(@RequestHeader("Authorization") String authorization, Pageable pageable) {
        return service.findByAuthUserId(currentUserId(authorization), pageable);
    }

    @PostMapping("/me/purchase")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseSubscriptionResponse purchase(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody PurchaseSubscriptionRequest request
    ) {
        return service.purchase(currentUserId(authorization), request);
    }

    private Long currentUserId(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        return jwtService.userId(token);
    }
}
