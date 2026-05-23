package com.fithub.booking.controller;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.entity.Notification;
import com.fithub.booking.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends AbstractCrudController<Notification> {
    private final NotificationService service;
    private final JwtService jwtService;

    public NotificationController(NotificationService service, JwtService jwtService) {
        super(service);
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public Page<Notification> mine(@RequestHeader("Authorization") String authorization, Pageable pageable) {
        return service.findByAuthUserId(currentUserId(authorization), pageable);
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        return service.markRead(id);
    }

    @PutMapping("/me/{id}/read")
    public Notification markReadForCurrentUser(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        return service.markReadForAuthUser(currentUserId(authorization), id);
    }

    private Long currentUserId(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        return jwtService.userId(token);
    }
}
