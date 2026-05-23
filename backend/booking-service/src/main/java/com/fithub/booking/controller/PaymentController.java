package com.fithub.booking.controller;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.entity.Payment;
import com.fithub.booking.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController extends AbstractCrudController<Payment> {
    private final PaymentService service;
    private final JwtService jwtService;

    public PaymentController(PaymentService service, JwtService jwtService) {
        super(service);
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public Page<Payment> mine(@RequestHeader("Authorization") String authorization, Pageable pageable) {
        return service.findByAuthUserId(currentUserId(authorization), pageable);
    }

    private Long currentUserId(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        return jwtService.userId(token);
    }
}
