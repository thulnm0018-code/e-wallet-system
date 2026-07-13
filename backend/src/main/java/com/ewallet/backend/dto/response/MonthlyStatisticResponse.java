package com.ewallet.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MonthlyStatisticResponse {

    private String month;

    private long transactionCount;

    private BigDecimal totalVolume;
}