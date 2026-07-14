package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SuspiciousActivityResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String reason;

    private String details;

    private LocalDateTime detectedAt;
}