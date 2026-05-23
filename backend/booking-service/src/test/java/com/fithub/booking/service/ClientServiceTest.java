package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fithub.booking.dto.ClientProfileRequest;
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

    @Test
    void upsertCurrentClientCreatesMissingClient() {
        when(repository.findByAuthUserId(42L)).thenReturn(Optional.empty());
        when(repository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientService service = new ClientService(repository);

        Client client = service.upsertCurrentClient(42L, new ClientProfileRequest(
            "Laurentiu",
            "Vrednicu",
            "laur@example.com",
            "0700000000"
        ));

        assertThat(client.getAuthUserId()).isEqualTo(42L);
        assertThat(client.getFirstName()).isEqualTo("Laurentiu");
        assertThat(client.getLastName()).isEqualTo("Vrednicu");
    }
}
