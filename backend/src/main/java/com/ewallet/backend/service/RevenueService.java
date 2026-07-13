package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.MonthlyRevenueResponse;
import com.ewallet.backend.dto.response.RevenueDashboardResponse;

import java.util.List;

public interface RevenueService {

    RevenueDashboardResponse getDashboard();

    List<MonthlyRevenueResponse>
    getMonthlyRevenue(Integer year);
}