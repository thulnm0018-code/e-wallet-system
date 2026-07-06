package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.ChangePasswordRequest;
import com.ewallet.backend.dto.request.UpdateProfileRequest;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminTransactionResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.ReceiverLookupResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<ReceiverLookupResponse>> getUserByPhone(@PathVariable String phone) {
        ReceiverLookupResponse response = userService.getUserByPhone(phone);
        return ResponseEntity.ok(
                ApiResponse.<ReceiverLookupResponse>builder()
                        .message("User found")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .message("Profile updated successfully")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Password changed successfully")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAdminUsers() {
        return ResponseEntity.ok(
                ApiResponse.<List<AdminUserResponse>>builder()
                        .message("Admin users fetched")
                        .data(userService.getAdminUsers())
                        .build()
        );
    }

    @GetMapping("/admin/transactions")
    public ResponseEntity<ApiResponse<List<AdminTransactionResponse>>> getAdminTransactions() {
        return ResponseEntity.ok(
                ApiResponse.<List<AdminTransactionResponse>>builder()
                        .message("Admin transactions fetched")
                        .data(userService.getAdminTransactions())
                        .build()
        );
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        return ResponseEntity.ok(
                ApiResponse.<AdminDashboardResponse>builder()
                        .message("Admin dashboard fetched")
                        .data(userService.getAdminDashboard())
                        .build()
        );
    }
}