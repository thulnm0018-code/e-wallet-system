package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueDashboardResponse {

    private BigDecimal totalRevenue;

    private BigDecimal currentMonthRevenue;

    private Long totalTransactions;
}