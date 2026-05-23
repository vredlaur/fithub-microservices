package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fithub.booking.entity.Client;
import com.fithub.booking.repository.ClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @Mock
    ClientRepository repository;

    @Test
    void findByAuthUserIdReturnsLinkedClient() {
        Client client = new Client();
        client.setId(5L);
        client.setAuthUserId(42L);
        when(repository.findByAuthUserId(42L)).thenReturn(Optional.of(client));

        ClientService service = new ClientService(repository);

        assertThat(service.findByAuthUserId(42L).getId()).isEqualTo(5L);
    }
}
