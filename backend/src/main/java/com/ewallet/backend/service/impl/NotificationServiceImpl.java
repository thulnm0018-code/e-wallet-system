package com.ewallet.backend.service.impl;

import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.dto.response.NotificationResponse;
import com.ewallet.backend.entity.Notification;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.NotificationRepository;
import com.ewallet.backend.service.NotificationService;
import com.ewallet.backend.security.service.CurrentUserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
        private final CurrentUserService currentUserService;

        public NotificationServiceImpl(
                        NotificationRepository notificationRepository,
                        CurrentUserService currentUserService
        ) {
                this.notificationRepository = notificationRepository;
                this.currentUserService = currentUserService;
        }

    @Override
    public void createNotification(
            User user,
            String title,
            String content
    ) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    @Override
@Transactional(readOnly = true)
public List<NotificationResponse> getMyNotifications() {
    Long userId = currentUserService.getCurrentUserId();

    return notificationRepository
            .findByUser_IdOrderByCreatedAtDesc(userId)
            .stream()
            .map(notification ->
                    NotificationResponse.builder()
                            .id(notification.getId())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .read(notification.isRead())
                            .createdAt(notification.getCreatedAt())
                            .build()
            )
            .toList();
}

@Override
@Transactional
public void markAsRead(Long notificationId) {
    Long userId = currentUserService.getCurrentUserId();

    Notification notification = notificationRepository
            .findByIdAndUser_Id(notificationId, userId)
            .orElseThrow(() -> new NotFoundException("Notification not found"));

    notification.setRead(true);

    notificationRepository.save(notification);
}    
}