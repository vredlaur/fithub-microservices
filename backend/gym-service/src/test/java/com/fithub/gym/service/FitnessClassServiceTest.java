package com.fithub.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.gym.entity.FitnessClass;
import com.fithub.gym.exception.InvalidOperationException;
import com.fithub.gym.repository.FitnessClassRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FitnessClassServiceTest {
    @Mock
    FitnessClassRepository repository;
    @InjectMocks
    FitnessClassService service;

    @Test
    void createInitializesAvailableSlotsFromCapacity() {
        FitnessClass fitnessClass = new FitnessClass();
        fitnessClass.setName("HIIT");
        fitnessClass.setCapacity(12);
        fitnessClass.setStartTime(LocalDateTime.now().plusDays(1));
        fitnessClass.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        when(repository.save(any(FitnessClass.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FitnessClass saved = service.create(fitnessClass);

        assertThat(saved.getAvailableSlots()).isEqualTo(12);
    }

    @Test
    void createRejectsEndBeforeStart() {
        FitnessClass fitnessClass = new FitnessClass();
        fitnessClass.setName("Yoga");
        fitnessClass.setCapacity(8);
        fitnessClass.setStartTime(LocalDateTime.now().plusDays(1));
        fitnessClass.setEndTime(LocalDateTime.now().plusDays(1).minusHours(1));

        assertThatThrownBy(() -> service.create(fitnessClass))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void updateCapsAvailableSlotsToCapacity() {
        FitnessClass current = classWithSlots(1L, 10, 5, "SCHEDULED");
        FitnessClass updated = classWithSlots(null, 8, 20, "SCHEDULED");
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(any(FitnessClass.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FitnessClass saved = service.update(1L, updated);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getAvailableSlots()).isEqualTo(8);
    }

    @Test
    void hasAvailabilityRequiresScheduledClassWithSlots() {
        when(repository.findById(1L)).thenReturn(Optional.of(classWithSlots(1L, 10, 1, "SCHEDULED")));
        when(repository.findById(2L)).thenReturn(Optional.of(classWithSlots(2L, 10, 0, "SCHEDULED")));
        when(repository.findById(3L)).thenReturn(Optional.of(classWithSlots(3L, 10, 5, "CANCELLED")));

        assertThat(service.hasAvailability(1L)).isTrue();
        assertThat(service.hasAvailability(2L)).isFalse();
        assertThat(service.hasAvailability(3L)).isFalse();
    }

    @Test
    void reserveSlotDecrementsAvailableSlots() {
        FitnessClass fitnessClass = classWithSlots(1L, 10, 3, "SCHEDULED");
        when(repository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        when(repository.save(fitnessClass)).thenReturn(fitnessClass);

        FitnessClass saved = service.reserveSlot(1L);

        assertThat(saved.getAvailableSlots()).isEqualTo(2);
        verify(repository).save(fitnessClass);
    }

    @Test
    void reserveSlotRejectsUnavailableClass() {
        FitnessClass fitnessClass = classWithSlots(1L, 10, 0, "SCHEDULED");
        when(repository.findById(1L)).thenReturn(Optional.of(fitnessClass));

        assertThatThrownBy(() -> service.reserveSlot(1L))
            .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void releaseSlotDoesNotExceedCapacity() {
        FitnessClass full = classWithSlots(1L, 10, 10, "SCHEDULED");
        when(repository.findById(1L)).thenReturn(Optional.of(full));
        when(repository.save(full)).thenReturn(full);

        FitnessClass saved = service.releaseSlot(1L);

        assertThat(saved.getAvailableSlots()).isEqualTo(10);
    }

    private FitnessClass classWithSlots(Long id, int capacity, int slots, String status) {
        FitnessClass fitnessClass = new FitnessClass();
        fitnessClass.setId(id);
        fitnessClass.setName("HIIT");
        fitnessClass.setCapacity(capacity);
        fitnessClass.setAvailableSlots(slots);
        fitnessClass.setStatus(status);
        fitnessClass.setStartTime(LocalDateTime.now().plusDays(1));
        fitnessClass.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        return fitnessClass;
    }
}
