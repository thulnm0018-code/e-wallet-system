package com.ewallet.backend.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.NotificationResponse;
import com.ewallet.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
    getMyNotifications() {

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .message(
                                "Notifications fetched successfully"
                        )
                        .data(
                                notificationService
                                        .getMyNotifications()
                        )
                        .build()
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>>
    markAsRead(
            @PathVariable Long notificationId
    ) {

        notificationService.markAsRead(
                notificationId
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message(
                                "Notification marked as read"
                        )
                        .build()
        );
    }
}