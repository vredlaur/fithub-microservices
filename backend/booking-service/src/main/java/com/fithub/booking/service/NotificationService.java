package com.fithub.booking.service;

import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.Notification;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService extends CrudService<Notification> {
    private final NotificationRepository repository;
    private final ClientRepository clientRepository;

    public NotificationService(NotificationRepository repository, ClientRepository clientRepository) {
        super(repository, "Notificarea");
        this.repository = repository;
        this.clientRepository = clientRepository;
    }

    public Notification markRead(Long id) {
        Notification notification = findById(id);
        notification.setRead(true);
        return repository.save(notification);
    }

    public Page<Notification> findByAuthUserId(Long authUserId, Pageable pageable) {
        Client client = findClientByAuthUserId(authUserId);
        return repository.findByClientId(client.getId(), pageable);
    }

    public Notification markReadForAuthUser(Long authUserId, Long id) {
        Client client = findClientByAuthUserId(authUserId);
        Notification notification = repository.findByIdAndClientId(id, client.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Notificarea nu a fost gasita pentru contul curent."));
        notification.setRead(true);
        return repository.save(notification);
    }

    private Client findClientByAuthUserId(Long authUserId) {
        return clientRepository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu exista. Completeaza profilul de client inainte de abonament sau rezervare."));
    }
}
