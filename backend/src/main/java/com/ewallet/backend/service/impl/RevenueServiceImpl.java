package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.response.MonthlyRevenueResponse;
import com.ewallet.backend.dto.response.RevenueDashboardResponse;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.service.RevenueService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RevenueServiceImpl
        implements RevenueService {

    private final TransactionRepository transactionRepository;

    public RevenueServiceImpl(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RevenueDashboardResponse getDashboard() {

        LocalDate now = LocalDate.now();

        return RevenueDashboardResponse.builder()
                .totalRevenue(
                        transactionRepository.getTotalRevenue()
                )
                .currentMonthRevenue(
                        transactionRepository.getRevenueByMonth(
                                now.getYear(),
                                now.getMonthValue()
                        )
                )
                .totalTransactions(
                        transactionRepository
                                .getTotalCompletedTransactions()
                )
                .build();
    }

    @Override
    public List<MonthlyRevenueResponse>
    getMonthlyRevenue(Integer year) {

        return transactionRepository
                .getMonthlyRevenue(year)
                .stream()
                .map(row ->
                        MonthlyRevenueResponse.builder()
                                .month((Integer) row[0])
                                .revenue((BigDecimal) row[1])
                                .build()
                )
                .toList();
    }
}