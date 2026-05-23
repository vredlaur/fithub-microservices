package com.fithub.booking.service;

import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.repository.SubscriptionTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionTypeService extends CrudService<SubscriptionType> {
    public SubscriptionTypeService(SubscriptionTypeRepository repository) {
        super(repository, "Tipul de abonament");
    }
}
