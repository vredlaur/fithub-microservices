package com.fithub.booking.service;

import com.fithub.booking.dto.PurchaseSubscriptionRequest;
import com.fithub.booking.dto.PurchaseSubscriptionResponse;
import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.entity.Payment;
import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.PaymentRepository;
import com.fithub.booking.repository.SubscriptionTypeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientSubscriptionService extends CrudService<ClientSubscription> {
    private final ClientSubscriptionRepository repository;
    private final ClientRepository clientRepository;
    private final SubscriptionTypeRepository subscriptionTypeRepository;
    private final PaymentRepository paymentRepository;

    public ClientSubscriptionService(
        ClientSubscriptionRepository repository,
        ClientRepository clientRepository,
        SubscriptionTypeRepository subscriptionTypeRepository,
        PaymentRepository paymentRepository
    ) {
        super(repository, "Abonamentul");
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.subscriptionTypeRepository = subscriptionTypeRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ClientSubscription create(ClientSubscription entity) {
        validateDates(entity);
        return super.create(entity);
    }

    @Override
    public ClientSubscription update(Long id, ClientSubscription entity) {
        validateDates(entity);
        return super.update(id, entity);
    }

    public Page<ClientSubscription> findByAuthUserId(Long authUserId, Pageable pageable) {
        Client client = findClientByAuthUserId(authUserId);
        return repository.findByClientId(client.getId(), pageable);
    }

    @Transactional
    public PurchaseSubscriptionResponse purchase(Long authUserId, PurchaseSubscriptionRequest request) {
        Client client = findClientByAuthUserId(authUserId);
        SubscriptionType type = subscriptionTypeRepository.findById(request.subscriptionTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("Tipul de abonament nu a fost gasit."));
        if (!type.isActive()) {
            throw new InvalidOperationException("Tipul de abonament nu este activ.");
        }

        LocalDate start = LocalDate.now();
        ClientSubscription subscription = new ClientSubscription();
        subscription.setClient(client);
        subscription.setSubscriptionType(type);
        subscription.setStartDate(start);
        subscription.setEndDate(start.plusDays(type.getDurationDays()));
        subscription.setStatus("ACTIVE");
        subscription = repository.save(subscription);

        Payment payment = new Payment();
        payment.setClient(client);
        payment.setClientSubscription(subscription);
        payment.setAmount(type.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PAID");
        payment.setMethod(normalizeMethod(request.paymentMethod()));
        payment = paymentRepository.save(payment);

        return new PurchaseSubscriptionResponse(subscription, payment);
    }

    private void validateDates(ClientSubscription entity) {
        if (entity.getStartDate() != null && entity.getEndDate() != null && !entity.getEndDate().isAfter(entity.getStartDate())) {
            throw new InvalidOperationException("Data de final trebuie sa fie dupa data de inceput.");
        }
    }

    private Client findClientByAuthUserId(Long authUserId) {
        return clientRepository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu exista. Completeaza profilul de client inainte de abonament sau rezervare."));
    }

    private String normalizeMethod(String method) {
        return method == null || method.isBlank() ? "CARD" : method.trim().toUpperCase();
    }
}
