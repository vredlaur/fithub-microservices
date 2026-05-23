package com.fithub.booking.service;

import com.fithub.booking.entity.Payment;
import com.fithub.booking.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends CrudService<Payment> {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(PaymentRepository repository) {
        super(repository, "Plata");
    }

    @Override
    public Payment create(Payment entity) {
        Payment saved = super.create(entity);
        log.info("Created payment {}", saved.getId());
        return saved;
    }
}
