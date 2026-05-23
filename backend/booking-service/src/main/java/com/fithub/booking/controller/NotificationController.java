package com.fithub.booking.controller;

import com.fithub.booking.entity.Notification;
import com.fithub.booking.service.NotificationService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends AbstractCrudController<Notification> {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        super(service);
        this.service = service;
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        return service.markRead(id);
    }
}
