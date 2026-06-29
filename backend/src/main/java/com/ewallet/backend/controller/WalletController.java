package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.TransferInitiateRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.dto.response.TransferOtpResponse;
import com.ewallet.backend.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/transfer/initiate")
    public ResponseEntity<ApiResponse<TransferOtpResponse>> initiateTransfer(@Valid @RequestBody TransferInitiateRequest request) {
        log.info("[WALLET-CONTROLLER] POST /transfer/initiate called with receiver: {}, amount: {}", request.getReceiverPhone(), request.getAmount());
        TransferOtpResponse response = walletService.initiateTransfer(request);
        log.info("[WALLET-CONTROLLER] OTP response generated successfully");
        return ResponseEntity.ok(
                ApiResponse.<TransferOtpResponse>builder()
                        .message("Transfer initiated. OTP sent.")
                        .data(response)
                        .build()
        );
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

   @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = walletService.depositMoney(request);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Deposit successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request) { 

        TransactionResponse response = walletService.withdrawMoney(request);

        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .message("Withdraw successful")
                        .data(response)
                        .build()
        );
    }
}
