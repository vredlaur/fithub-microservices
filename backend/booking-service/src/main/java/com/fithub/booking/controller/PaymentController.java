package com.fithub.booking.controller;

import com.fithub.booking.entity.Payment;
import com.fithub.booking.service.PaymentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController extends AbstractCrudController<Payment> {
    public PaymentController(PaymentService service) {
        super(service);
    }
}
