package com.ewallet.backend.repository;

import com.ewallet.backend.entity.AuditLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog>
    findAllByOrderByCreatedAtDesc();

    @Query("""
    SELECT a
    FROM AuditLog a
    LEFT JOIN FETCH a.user
    ORDER BY a.createdAt DESC
""")
List<AuditLog> findAllWithUser();

}
