package com.ewallet.backend.repository.projection;

import java.math.BigDecimal;

public interface MonthlyStatisticProjection {

    Integer getYear();

    Integer getMonth();

    Long getTransactionCount();

    BigDecimal getTotalVolume();
}