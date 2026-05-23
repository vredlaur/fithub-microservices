package com.fithub.booking.service;

import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientSubscriptionService extends CrudService<ClientSubscription> {
    public ClientSubscriptionService(ClientSubscriptionRepository repository) {
        super(repository, "Abonamentul");
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

    private void validateDates(ClientSubscription entity) {
        if (entity.getStartDate() != null && entity.getEndDate() != null && !entity.getEndDate().isAfter(entity.getStartDate())) {
            throw new InvalidOperationException("Data de final trebuie sa fie dupa data de inceput.");
        }
    }
}
