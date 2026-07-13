package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {

        return ResponseEntity.ok(
                ApiResponse.<AdminDashboardResponse>builder()
                        .message("Dashboard statistics fetched successfully")
                        .data(
                                adminService.getDashboard()
                        )
                        .build()
        );
    }
}