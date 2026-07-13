package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.MonthlyStatisticResponse;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.service.AdminService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    @GetMapping("/dashboard/monthly")
public ResponseEntity<ApiResponse<List<MonthlyStatisticResponse>>>
getMonthlyStatistics() {

    return ResponseEntity.ok(
            ApiResponse.<List<MonthlyStatisticResponse>>builder()
                    .message("Monthly statistics fetched successfully")
                    .data(
                            adminService.getMonthlyStatistics()
                    )
                    .build()
    );
}

@GetMapping("/users")
public ResponseEntity<
        ApiResponse<Page<AdminUserResponse>>
        > searchUsers(

        @RequestParam(required = false)
        String keyword,

        @RequestParam(required = false)
        UserStatus status,

        @RequestParam(defaultValue = "0")
        int page,

        @RequestParam(defaultValue = "10")
        int size
) {

    Pageable pageable =
            PageRequest.of(page, size);

    return ResponseEntity.ok(
            ApiResponse.<Page<AdminUserResponse>>builder()
                    .message(
                            "Users fetched successfully"
                    )
                    .data(
                            adminService.searchUsers(
                                    keyword,
                                    status,
                                    pageable
                            )
                    )
                    .build()
    );
}

    @DeleteMapping("/users/{userId}")
public ResponseEntity<ApiResponse<Void>> deleteUser(
        @PathVariable Long userId
) {

    adminService.deleteUser(userId);

    return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                    .message("User deleted successfully")
                    .build()
    );
}

@PatchMapping("/users/{userId}/restore")
public ResponseEntity<ApiResponse<Void>> restoreUser(
        @PathVariable Long userId
) {

    adminService.restoreUser(userId);

    return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                    .message("User restored successfully")
                    .build()
    );
}

}