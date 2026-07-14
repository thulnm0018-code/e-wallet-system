package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WithdrawalRequestResponse {

    private Long id;

    private Long userId;

    private String userName;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createdAt;
}