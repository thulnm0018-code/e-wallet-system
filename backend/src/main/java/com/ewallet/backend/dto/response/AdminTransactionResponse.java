package com.ewallet.backend.dto.response;

import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminTransactionResponse {
    private Long id;
    private String transactionCode;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String paymentMethod;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public static AdminTransactionResponse fromEntity(Transaction transaction) {
        return AdminTransactionResponse.builder()
                .id(transaction.getId())
                .transactionCode(transaction.getTransactionCode())
                .sender(transaction.getSenderWallet() != null && transaction.getSenderWallet().getUser() != null
                        ? transaction.getSenderWallet().getUser().getName() : null)
                .receiver(transaction.getReceiverWallet() != null && transaction.getReceiverWallet().getUser() != null
                        ? transaction.getReceiverWallet().getUser().getName() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .approvedBy(transaction.getApprovedBy())
                .approvedAt(transaction.getApprovedAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
