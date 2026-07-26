package com.ewallet.backend.service.impl;

import com.ewallet.backend.configuration.RabbitConfig;
import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.service.RabbitProducerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitProducerServiceImpl
        implements RabbitProducerService {

    private static final Logger log = LoggerFactory.getLogger(RabbitProducerServiceImpl.class);

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
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_QUEUE,
                    message
            );
        } catch (Exception ex) {
            log.warn("Unable to publish notification message to RabbitMQ: {}", ex.getMessage());
        }
    }
}