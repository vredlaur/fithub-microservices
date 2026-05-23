package com.fithub.booking.service;

import com.fithub.booking.entity.Notification;
import com.fithub.booking.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService extends CrudService<Notification> {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        super(repository, "Notificarea");
        this.repository = repository;
    }

    public Notification markRead(Long id) {
        Notification notification = findById(id);
        notification.setRead(true);
        return repository.save(notification);
    }
}
