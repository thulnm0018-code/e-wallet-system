package com.ewallet.backend.dto.response;

import com.ewallet.backend.enums.AuditAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {

    private Long id;

    private Long userId;

    private String userName;

    private AuditAction action;

    private String description;

    private LocalDateTime createdAt;
}