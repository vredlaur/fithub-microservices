package com.fithub.booking.service;

import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.Payment;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends CrudService<Payment> {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository repository;
    private final ClientRepository clientRepository;

    public PaymentService(PaymentRepository repository, ClientRepository clientRepository) {
        super(repository, "Plata");
        this.repository = repository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Payment create(Payment entity) {
        Payment saved = super.create(entity);
        log.info("Created payment {}", saved.getId());
        return saved;
    }

    public Page<Payment> findByAuthUserId(Long authUserId, Pageable pageable) {
        Client client = clientRepository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu exista. Completeaza profilul de client inainte de abonament sau rezervare."));
        return repository.findByClientId(client.getId(), pageable);
    }
}
