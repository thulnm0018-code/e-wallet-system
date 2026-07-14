package com.ewallet.backend.repository;

import com.ewallet.backend.entity.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog>
    findAllByOrderByCreatedAtDesc();
}
