package com.fithub.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fithub.booking.entity.Notification;
import com.fithub.booking.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    NotificationRepository repository;

    @Test
    void markReadSetsReadFlag() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test");
        notification.setMessage("Message");
        when(repository.findById(1L)).thenReturn(Optional.of(notification));
        when(repository.save(notification)).thenReturn(notification);

        NotificationService service = new NotificationService(repository);

        assertThat(service.markRead(1L).isRead()).isTrue();
    }
}
