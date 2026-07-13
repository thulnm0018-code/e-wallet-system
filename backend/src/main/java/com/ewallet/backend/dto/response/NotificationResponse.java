package com.ewallet.backend.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String content;

    private boolean read;

    private LocalDateTime createdAt;
}