package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.LinkBankAccountRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.LinkedBankAccountResponse;
import com.ewallet.backend.service.LinkedBankAccountService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
public class LinkedBankAccountController {

    private final LinkedBankAccountService
            linkedBankAccountService;

    public LinkedBankAccountController(
            LinkedBankAccountService linkedBankAccountService
    ) {
        this.linkedBankAccountService =
                linkedBankAccountService;
    }

    @PostMapping
    public ApiResponse<Void> linkBankAccount(
            @Valid
            @RequestBody LinkBankAccountRequest request
    ) {

        linkedBankAccountService.linkBankAccount(
                request
        );

        return ApiResponse.<Void>builder()
                .message(
                        "Bank account linked successfully"
                )
                .build();
    }

    @GetMapping
    public ApiResponse<
            List<LinkedBankAccountResponse>>
    getMyLinkedAccounts() {

        return ApiResponse
                .<List<LinkedBankAccountResponse>>
                        builder()
                .data(
                        linkedBankAccountService
                                .getMyLinkedAccounts()
                )
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> unlinkAccount(
            @PathVariable Long id
    ) {

        linkedBankAccountService.unlinkAccount(
                id
        );

        return ApiResponse.<Void>builder()
                .message(
                        "Bank account unlinked successfully"
                )
                .build();
    }
}