package com.fithub.booking.repository;

import com.fithub.booking.entity.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByAuthUserId(Long authUserId);
}
