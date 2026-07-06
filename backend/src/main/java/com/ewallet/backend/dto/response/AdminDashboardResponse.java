package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long activeWallets;
    private BigDecimal totalVolume;
    private long pendingReviews;
}
