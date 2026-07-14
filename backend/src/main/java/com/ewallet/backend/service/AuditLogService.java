package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.AuditLogResponse;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.AuditAction;

import java.util.List;

public interface AuditLogService {

    void log(
            User user,
            AuditAction action,
            String description
    );

    List<AuditLogResponse> getAllLogs();
}