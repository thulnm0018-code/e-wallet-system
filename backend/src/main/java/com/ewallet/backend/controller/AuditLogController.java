package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.AuditLogResponse;
import com.ewallet.backend.service.AuditLogService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService
    ) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> getLogs() {

        return ApiResponse
                .<List<AuditLogResponse>>builder()
                .data(auditLogService.getAllLogs())
                .build();
    }
}