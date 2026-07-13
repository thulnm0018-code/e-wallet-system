package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.NotificationResponse;
import com.ewallet.backend.entity.User;

import java.util.List;

public interface NotificationService {

    void createNotification(
            User user,
            String title,
            String content
    );

    List<NotificationResponse> getMyNotifications();

    void markAsRead(Long notificationId);
}
