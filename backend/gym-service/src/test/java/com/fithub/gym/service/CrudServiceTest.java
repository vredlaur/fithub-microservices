package com.fithub.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.gym.entity.Location;
import com.fithub.gym.exception.ResourceNotFoundException;
import com.fithub.gym.repository.LocationRepository;
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
    LocationRepository repository;

    @Test
    void findAllDelegatesToRepository() {
        PageRequest pageable = PageRequest.of(0, 3);
        Page<Location> page = new PageImpl<>(java.util.List.of(location(1L)));
        when(repository.findAll(pageable)).thenReturn(page);

        TestLocationService service = new TestLocationService(repository);

        assertThat(service.findAll(pageable)).isSameAs(page);
    }

    @Test
    void findByIdReturnsEntityOrThrows() {
        Location location = location(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(location));
        when(repository.findById(9L)).thenReturn(Optional.empty());

        TestLocationService service = new TestLocationService(repository);

        assertThat(service.findById(1L)).isSameAs(location);
        assertThatThrownBy(() -> service.findById(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createClearsIdBeforeSaving() {
        Location location = location(5L);
        when(repository.save(location)).thenReturn(location);

        TestLocationService service = new TestLocationService(repository);

        assertThat(service.create(location).getId()).isNull();
    }

    @Test
    void updateRequiresExistingEntityAndSavesWithPathId() {
        Location location = location(null);
        when(repository.findById(7L)).thenReturn(Optional.of(location(7L)));
        when(repository.save(location)).thenReturn(location);

        TestLocationService service = new TestLocationService(repository);

        assertThat(service.update(7L, location).getId()).isEqualTo(7L);
    }

    @Test
    void deleteRemovesExistingEntity() {
        Location location = location(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(location));

        TestLocationService service = new TestLocationService(repository);
        service.delete(1L);

        verify(repository).delete(location);
    }

    private Location location(Long id) {
        Location location = new Location();
        location.setId(id);
        location.setName("Central");
        location.setAddress("Strada 1");
        location.setCity("Bucuresti");
        return location;
    }

    private static class TestLocationService extends CrudService<Location> {
        TestLocationService(LocationRepository repository) {
            super(repository, "Locatia");
        }
    }
}
