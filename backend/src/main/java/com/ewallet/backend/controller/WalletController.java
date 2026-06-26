package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = walletService.transferMoney(request);
        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Transfer successful")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory() {
        List<TransactionResponse> history = walletService.getMyHistory();
        return ResponseEntity.ok(
                ApiResponse.<List<TransactionResponse>>builder()
                        .message("Fetch transaction history successful")
                        .data(history)
                        .build()
        );
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance() {
        BigDecimal balance = walletService.getMyBalance();
        return ResponseEntity.ok(
                ApiResponse.<BigDecimal>builder()
                        .message("Fetch balance successful")
                        .data(balance)
                        .build()
        );
    }
}