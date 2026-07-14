package com.ewallet.backend.message;

import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NotificationConsumer.class
            );

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    public NotificationConsumer(
            NotificationService notificationService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @SuppressWarnings("null")
@RabbitListener(
            queues = "notification.queue"
    )
    public void receive(
            NotificationMessage message
    ) {

        User user =
                userRepository
                        .findById(
                                message.getUserId()
                        )
                        .orElse(null);

        if (user == null) {

            log.warn(
                    "User not found for notification. userId={}",
                    message.getUserId()
            );

            return;
        }

        notificationService.createNotification(
                user,
                message.getTitle(),
                message.getContent()
        );
    }
}