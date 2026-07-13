package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlyRevenueResponse {

    private Integer month;

    private BigDecimal revenue;
}