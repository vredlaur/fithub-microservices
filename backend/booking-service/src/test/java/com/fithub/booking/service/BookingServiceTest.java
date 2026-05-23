package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.booking.client.GymClient;
import com.fithub.booking.dto.BookingRequest;
import com.fithub.booking.entity.Booking;
import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.BookingRepository;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.NotificationRepository;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    ClientSubscriptionRepository subscriptionRepository;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    GymClient gymClient;
    @InjectMocks
    BookingService service;

    @Test
    void createReservesSlotAndStoresBookingWhenSubscriptionIsActive() {
        Client client = new Client();
        client.setId(1L);
        ClientSubscription subscription = new ClientSubscription();
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findFirstByClientIdAndStatusAndEndDateAfter(1L, "ACTIVE", LocalDate.now()))
            .thenReturn(Optional.of(subscription));
        when(gymClient.availability(2L)).thenReturn(Map.of("available", true));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.create(new BookingRequest(1L, 2L));

        assertThat(booking.getFitnessClassId()).isEqualTo(2L);
        verify(gymClient).reserveSlot(2L);
        verify(notificationRepository).save(any());
    }

    @Test
    void createForAuthUserUsesLinkedClient() {
        Client client = new Client();
        client.setId(1L);
        client.setAuthUserId(42L);
        when(clientRepository.findByAuthUserId(42L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findFirstByClientIdAndStatusAndEndDateAfter(1L, "ACTIVE", LocalDate.now()))
            .thenReturn(Optional.of(new ClientSubscription()));
        when(gymClient.availability(2L)).thenReturn(Map.of("available", true));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.createForAuthUser(42L, 2L);

        assertThat(booking.getClient().getAuthUserId()).isEqualTo(42L);
        verify(gymClient).reserveSlot(2L);
    }

    @Test
    void deleteForAuthUserRejectsForeignBooking() {
        Client currentClient = new Client();
        currentClient.setId(1L);
        Client otherClient = new Client();
        otherClient.setId(2L);
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setClient(otherClient);
        booking.setFitnessClassId(2L);
        when(clientRepository.findByAuthUserId(42L)).thenReturn(Optional.of(currentClient));
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.deleteForAuthUser(42L, 10L))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void createRejectsMissingClient() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new BookingRequest(99L, 2L)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRejectsClientWithoutActiveSubscription() {
        Client client = new Client();
        client.setId(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findFirstByClientIdAndStatusAndEndDateAfter(1L, "ACTIVE", LocalDate.now()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new BookingRequest(1L, 2L)))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void createRejectsUnavailableClass() {
        Client client = new Client();
        client.setId(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.findFirstByClientIdAndStatusAndEndDateAfter(1L, "ACTIVE", LocalDate.now()))
            .thenReturn(Optional.of(new ClientSubscription()));
        when(gymClient.availability(2L)).thenReturn(Map.of("available", false));

        assertThatThrownBy(() -> service.create(new BookingRequest(1L, 2L)))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void deleteReleasesSlotAndDeletesBooking() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setFitnessClassId(2L);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        service.delete(10L);

        verify(gymClient).releaseSlot(2L);
        verify(bookingRepository).delete(booking);
    }
}
