package com.fithub.booking.service;

import com.fithub.booking.entity.Client;
import com.fithub.booking.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService extends CrudService<Client> {
    public ClientService(ClientRepository repository) {
        super(repository, "Clientul");
    }
}
