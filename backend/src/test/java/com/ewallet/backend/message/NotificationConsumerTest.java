package com.ewallet.backend.message;

import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    void shouldCreateNotificationWhenMessageReceived() {
        User user = new User();
        user.setId(1L);

        NotificationMessage message = NotificationMessage.builder()
                .userId(1L)
                .title("Transfer Success")
                .content("You transferred 100 VND")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        consumer.receive(message);

        verify(notificationService).createNotification(user, "Transfer Success", "You transferred 100 VND");
    }
}
