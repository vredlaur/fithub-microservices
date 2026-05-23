package com.fithub.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fithub.gym.entity.FitnessClass;
import com.fithub.gym.repository.FitnessClassRepository;
import java.time.LocalDateTime;
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
}
