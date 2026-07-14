package com.ewallet.backend.service;

import com.ewallet.backend.dto.message.NotificationMessage;

public interface RabbitProducerService {

    void sendNotification(
            NotificationMessage message
    );
}