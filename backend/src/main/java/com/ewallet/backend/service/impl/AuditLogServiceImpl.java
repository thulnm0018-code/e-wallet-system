package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.response.AuditLogResponse;
import com.ewallet.backend.entity.AuditLog;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.AuditAction;
import com.ewallet.backend.repository.AuditLogRepository;
import com.ewallet.backend.service.AuditLogService;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl
        implements AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogServiceImpl(
            AuditLogRepository repository
    ) {
        this.repository = repository;
    }

    @SuppressWarnings("null")
@Override
    public void log(
            User user,
            AuditAction action,
            String description
    ) {

        repository.save(
                AuditLog.builder()
                        .user(user)
                        .action(action)
                        .description(description)
                        .build()
        );
    }

    @Override
public List<AuditLogResponse> getAllLogs() {

    return repository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(log ->
                    AuditLogResponse.builder()
                            .id(log.getId())
                            .userId(
                                    log.getUser() != null
                                            ? log.getUser().getId()
                                            : null
                            )
                            .userName(
                                    log.getUser() != null
                                            ? log.getUser().getName()
                                            : null
                            )
                            .action(log.getAction())
                            .description(log.getDescription())
                            .createdAt(log.getCreatedAt())
                            .build()
            )
            .toList();
}
}