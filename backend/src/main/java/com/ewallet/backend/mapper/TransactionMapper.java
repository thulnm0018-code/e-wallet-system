package com.ewallet.backend.mapper;

import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(
            Transaction tx) {

        return TransactionResponse.builder()
                .id(tx.getId())
                .transactionCode(
                        tx.getTransactionCode()
                )
                .senderPhone(
                        tx.getSenderWallet() != null
                                ? tx.getSenderWallet()
                                    .getUser()
                                    .getPhone()
                                : "SYSTEM"
                )
                .receiverPhone(
                        tx.getReceiverWallet() != null
                                ? tx.getReceiverWallet()
                                    .getUser()
                                    .getPhone()
                                : "SYSTEM"
                )
                .amount(tx.getAmount())
                .message(tx.getMessage())
                .status(tx.getStatus())
                .type(tx.getType())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}