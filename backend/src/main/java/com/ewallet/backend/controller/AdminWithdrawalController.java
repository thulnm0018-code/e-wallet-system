package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.WithdrawalRequestResponse;
import com.ewallet.backend.service.WithdrawalApprovalService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdraw-requests")
public class AdminWithdrawalController {

    private final WithdrawalApprovalService service;

    public AdminWithdrawalController(
            WithdrawalApprovalService service
    ) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<WithdrawalRequestResponse>>
    getAll() {

        return ApiResponse
                .<List<WithdrawalRequestResponse>>
                        builder()
                .data(
                        service.getAllRequests()
                )
                .build();
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<String> approve(
            @PathVariable Long id
    ) {

        service.approve(id);

        return ApiResponse.<String>builder()
                .data("Approved")
                .build();
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<String> reject(
            @PathVariable Long id
    ) {

        service.reject(id);

        return ApiResponse.<String>builder()
                .data("Rejected")
                .build();
    }
}