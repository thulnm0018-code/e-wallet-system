package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final WalletService walletService;

    public TransactionController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getMyTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<TransactionResponse> history = walletService.getMyHistory(type, startDate, endDate, page, size);
        return ResponseEntity.ok(
                ApiResponse.<List<TransactionResponse>>builder()
                        .message("Fetch transaction history successful")
                        .data(history)
                        .build()
        );
    }

    @GetMapping("/{id}")
public ResponseEntity<ApiResponse<TransactionResponse>>
getTransactionDetail(@PathVariable Long id) {

    return ResponseEntity.ok(
            ApiResponse.<TransactionResponse>builder()
                    .message("Fetch transaction detail successful")
                    .data(walletService.getTransactionDetail(id))
                    .build()
    );
}
}
