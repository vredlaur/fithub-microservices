package com.fithub.booking.service;

import com.fithub.booking.client.GymClient;
import com.fithub.booking.dto.BookingRequest;
import com.fithub.booking.entity.Booking;
import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.Notification;
import com.fithub.booking.exception.InvalidOperationException;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.BookingRepository;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final ClientSubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final GymClient gymClient;

    public BookingService(
        BookingRepository bookingRepository,
        ClientRepository clientRepository,
        ClientSubscriptionRepository subscriptionRepository,
        NotificationRepository notificationRepository,
        GymClient gymClient
    ) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.gymClient = gymClient;
    }

    public Page<Booking> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    public Page<Booking> findByAuthUserId(Long authUserId, Pageable pageable) {
        Client client = findClientByAuthUserId(authUserId);
        return bookingRepository.findByClientId(client.getId(), pageable);
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rezervarea nu a fost gasita."));
    }

    @Transactional
    public Booking create(BookingRequest request) {
        Client client = clientRepository.findById(request.clientId())
            .orElseThrow(() -> new ResourceNotFoundException("Clientul nu a fost gasit."));
        return createForClient(client, request.fitnessClassId());
    }

    @Transactional
    public Booking createForAuthUser(Long authUserId, Long fitnessClassId) {
        Client client = findClientByAuthUserId(authUserId);
        return createForClient(client, fitnessClassId);
    }

    private Booking createForClient(Client client, Long fitnessClassId) {
        subscriptionRepository.findFirstByClientIdAndStatusAndEndDateAfter(client.getId(), "ACTIVE", LocalDate.now())
            .orElseThrow(() -> new InvalidOperationException("Clientul nu are abonament activ."));

        Map<String, Object> availability = gymClient.availability(fitnessClassId);
        if (!Boolean.TRUE.equals(availability.get("available"))) {
            log.info("Booking failed for client {} and class {}: no availability", client.getId(), fitnessClassId);
            throw new InvalidOperationException("Nu mai sunt locuri disponibile pentru clasa aleasa.");
        }

        gymClient.reserveSlot(fitnessClassId);

        Booking booking = new Booking();
        booking.setClient(client);
        booking.setFitnessClassId(fitnessClassId);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("CONFIRMED");
        Booking saved = bookingRepository.save(booking);

        Notification notification = new Notification();
        notification.setClient(client);
        notification.setTitle("Rezervare confirmata");
        notification.setMessage("Rezervarea pentru clasa #" + fitnessClassId + " a fost confirmata.");
        notificationRepository.save(notification);

        log.info("Created booking {} for client {}", saved.getId(), client.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Booking booking = findById(id);
        gymClient.releaseSlot(booking.getFitnessClassId());
        bookingRepository.delete(booking);
        log.info("Deleted booking {}", id);
    }

    @Transactional
    public void deleteForAuthUser(Long authUserId, Long id) {
        Client client = findClientByAuthUserId(authUserId);
        Booking booking = findById(id);
        if (!client.getId().equals(booking.getClient().getId())) {
            throw new InvalidOperationException("Rezervarea nu apartine contului curent.");
        }
        gymClient.releaseSlot(booking.getFitnessClassId());
        bookingRepository.delete(booking);
        log.info("Deleted booking {} for client {}", id, client.getId());
    }

    private Client findClientByAuthUserId(Long authUserId) {
        return clientRepository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Clientul asociat contului curent nu exista. Completeaza profilul de client inainte de abonament sau rezervare."));
    }
}
