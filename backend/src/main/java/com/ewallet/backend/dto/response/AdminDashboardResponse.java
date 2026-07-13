package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
    
    private long activeWallets;
    private long totalTransactions;
    private BigDecimal totalVolume;
    private BigDecimal totalRevenue;
    private long pendingReviews;
}
