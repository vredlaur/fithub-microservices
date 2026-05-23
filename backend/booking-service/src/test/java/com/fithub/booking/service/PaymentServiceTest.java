package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fithub.booking.entity.Payment;
import com.fithub.booking.repository.PaymentRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    PaymentRepository repository;

    @Test
    void createStoresPayment() {
        Payment payment = new Payment();
        payment.setId(10L);
        payment.setAmount(BigDecimal.valueOf(149.99));
        when(repository.save(payment)).thenReturn(payment);

        PaymentService service = new PaymentService(repository);

        Payment saved = service.create(payment);
        assertThat(saved.getId()).isNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("149.99");
    }
}
