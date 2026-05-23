package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.booking.entity.Client;
import com.fithub.booking.exception.ResourceNotFoundException;
import com.fithub.booking.repository.ClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CrudServiceTest {
    @Mock
    ClientRepository repository;

    @Test
    void findAllDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Client> page = new PageImpl<>(java.util.List.of(client(1L)));
        when(repository.findAll(pageable)).thenReturn(page);

        TestClientService service = new TestClientService(repository);

        assertThat(service.findAll(pageable)).isSameAs(page);
    }

    @Test
    void findByIdReturnsEntityOrThrows() {
        Client client = client(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(client));
        when(repository.findById(9L)).thenReturn(Optional.empty());

        TestClientService service = new TestClientService(repository);

        assertThat(service.findById(1L)).isSameAs(client);
        assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createClearsIdBeforeSaving() {
        Client client = client(3L);
        when(repository.save(client)).thenReturn(client);

        TestClientService service = new TestClientService(repository);

        assertThat(service.create(client).getId()).isNull();
    }

    @Test
    void updateRequiresExistingEntityAndSavesWithPathId() {
        Client client = client(null);
        when(repository.findById(7L)).thenReturn(Optional.of(client(7L)));
        when(repository.save(client)).thenReturn(client);

        TestClientService service = new TestClientService(repository);

        assertThat(service.update(7L, client).getId()).isEqualTo(7L);
    }

    @Test
    void deleteRemovesExistingEntity() {
        Client client = client(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(client));

        TestClientService service = new TestClientService(repository);
        service.delete(1L);

        verify(repository).delete(client);
    }

    private Client client(Long id) {
        Client client = new Client();
        client.setId(id);
        client.setAuthUserId(42L);
        client.setFirstName("Ana");
        client.setLastName("Pop");
        client.setEmail("ana@fithub.local");
        return client;
    }

    private static class TestClientService extends CrudService<Client> {
        TestClientService(ClientRepository repository) {
            super(repository, "Clientul");
        }
    }
}
