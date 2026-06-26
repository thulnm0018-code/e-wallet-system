package com.ewallet.backend.dto.response;

import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionCode;
    private String senderPhone;
    private String receiverPhone;
    private BigDecimal amount;
    private String message;
    private TransactionStatus status;
    private TransactionType type;
    private LocalDateTime createdAt;
}