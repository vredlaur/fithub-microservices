package com.fithub.booking.service;

import com.fithub.booking.entity.Client;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService extends CrudService<Client> {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        super(repository, "Clientul");
        this.repository = repository;
    }

    public Client findByAuthUserId(Long authUserId) {
        return repository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu a fost gasit."));
    }
}
