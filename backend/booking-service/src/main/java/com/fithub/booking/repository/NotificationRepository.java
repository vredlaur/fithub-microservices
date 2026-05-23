package com.fithub.booking.repository;

import com.fithub.booking.entity.Notification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByClientId(Long clientId, Pageable pageable);

    Optional<Notification> findByIdAndClientId(Long id, Long clientId);
}
