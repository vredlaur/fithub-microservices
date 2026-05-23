package com.fithub.booking.service;

import com.fithub.booking.dto.ClientProfileRequest;
import com.fithub.booking.entity.Client;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService extends CrudService<Client> {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        super(repository, "Clientul");
        this.repository = repository;
    }

    public Client findByAuthUserId(Long authUserId) {
        return repository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu exista. Completeaza profilul de client inainte de abonament sau rezervare."));
    }

    @Transactional
    public Client upsertCurrentClient(Long authUserId, ClientProfileRequest request) {
        Client client = repository.findByAuthUserId(authUserId).orElseGet(() -> {
            Client created = new Client();
            created.setAuthUserId(authUserId);
            return created;
        });
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        return repository.save(client);
    }
}
