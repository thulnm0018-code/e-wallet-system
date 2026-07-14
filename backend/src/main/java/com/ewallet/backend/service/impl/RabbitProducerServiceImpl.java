package com.ewallet.backend.service.impl;

import com.ewallet.backend.configuration.RabbitConfig;
import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.service.RabbitProducerService;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitProducerServiceImpl
        implements RabbitProducerService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitProducerServiceImpl(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendNotification(
            NotificationMessage message
    ) {

        rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_QUEUE,
                message
        );
    }
}