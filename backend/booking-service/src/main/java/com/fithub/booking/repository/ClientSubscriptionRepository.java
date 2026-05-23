package com.fithub.booking.repository;

import com.fithub.booking.entity.ClientSubscription;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscription, Long> {
    Page<ClientSubscription> findByClientId(Long clientId, Pageable pageable);

    Optional<ClientSubscription> findFirstByClientIdAndStatusAndEndDateAfter(Long clientId, String status, LocalDate date);
}
