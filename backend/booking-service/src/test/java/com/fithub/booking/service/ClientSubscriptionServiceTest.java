package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.repository.ClientSubscriptionRepository;
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

    @Test
    void createRejectsEndDateBeforeStartDate() {
        ClientSubscription subscription = subscription(LocalDate.now(), LocalDate.now().minusDays(1));
        ClientSubscriptionService service = new ClientSubscriptionService(repository);

        assertThatThrownBy(() -> service.create(subscription))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void createStoresValidSubscription() {
        ClientSubscription subscription = subscription(LocalDate.now(), LocalDate.now().plusDays(30));
        when(repository.save(subscription)).thenReturn(subscription);

        ClientSubscriptionService service = new ClientSubscriptionService(repository);

        assertThat(service.create(subscription)).isSameAs(subscription);
    }

    @Test
    void updateValidatesDatesBeforeSave() {
        ClientSubscription current = subscription(LocalDate.now(), LocalDate.now().plusDays(30));
        current.setId(1L);
        ClientSubscription update = subscription(LocalDate.now(), LocalDate.now().plusDays(60));
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(update)).thenReturn(update);

        ClientSubscriptionService service = new ClientSubscriptionService(repository);

        assertThat(service.update(1L, update).getId()).isEqualTo(1L);
    }

    private ClientSubscription subscription(LocalDate start, LocalDate end) {
        ClientSubscription subscription = new ClientSubscription();
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        subscription.setStatus("ACTIVE");
        return subscription;
    }
}
