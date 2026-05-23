package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fithub.booking.dto.PurchaseSubscriptionRequest;
import com.fithub.booking.dto.PurchaseSubscriptionResponse;
import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.entity.Payment;
import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.PaymentRepository;
import com.fithub.booking.repository.SubscriptionTypeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientSubscriptionServiceTest {
    @Mock
    ClientSubscriptionRepository repository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    SubscriptionTypeRepository subscriptionTypeRepository;
    @Mock
    PaymentRepository paymentRepository;

    @Test
    void createRejectsEndDateBeforeStartDate() {
        ClientSubscription subscription = subscription(LocalDate.now(), LocalDate.now().minusDays(1));
        ClientSubscriptionService service = service();

        assertThatThrownBy(() -> service.create(subscription))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void createStoresValidSubscription() {
        ClientSubscription subscription = subscription(LocalDate.now(), LocalDate.now().plusDays(30));
        when(repository.save(subscription)).thenReturn(subscription);

        ClientSubscriptionService service = service();

        assertThat(service.create(subscription)).isSameAs(subscription);
    }

    @Test
    void updateValidatesDatesBeforeSave() {
        ClientSubscription current = subscription(LocalDate.now(), LocalDate.now().plusDays(30));
        current.setId(1L);
        ClientSubscription update = subscription(LocalDate.now(), LocalDate.now().plusDays(60));
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(update)).thenReturn(update);

        ClientSubscriptionService service = service();

        assertThat(service.update(1L, update).getId()).isEqualTo(1L);
    }

    @Test
    void purchaseCreatesActiveSubscriptionAndPayment() {
        Client client = new Client();
        client.setId(2L);
        client.setAuthUserId(42L);
        SubscriptionType type = new SubscriptionType();
        type.setId(7L);
        type.setName("Standard");
        type.setPrice(BigDecimal.valueOf(149.99));
        type.setDurationDays(30);
        type.setActive(true);
        when(clientRepository.findByAuthUserId(42L)).thenReturn(Optional.of(client));
        when(subscriptionTypeRepository.findById(7L)).thenReturn(Optional.of(type));
        when(repository.save(any(ClientSubscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseSubscriptionResponse response = service().purchase(42L, new PurchaseSubscriptionRequest(7L, "card"));

        assertThat(response.subscription().getClient().getId()).isEqualTo(2L);
        assertThat(response.subscription().getStatus()).isEqualTo("ACTIVE");
        assertThat(response.payment().getAmount()).isEqualByComparingTo("149.99");
        assertThat(response.payment().getMethod()).isEqualTo("CARD");
    }

    private ClientSubscription subscription(LocalDate start, LocalDate end) {
        ClientSubscription subscription = new ClientSubscription();
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setStatus("ACTIVE");
        return subscription;
    }

    private ClientSubscriptionService service() {
        return new ClientSubscriptionService(repository, clientRepository, subscriptionTypeRepository, paymentRepository);
    }
}
