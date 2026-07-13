package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.MonthlyRevenueResponse;
import com.ewallet.backend.dto.response.RevenueDashboardResponse;
import com.ewallet.backend.service.RevenueService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/revenue")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(
            RevenueService revenueService
    ) {
        this.revenueService = revenueService;
    }

    @GetMapping
    public ApiResponse<RevenueDashboardResponse>
    getDashboard() {

        return ApiResponse
                .<RevenueDashboardResponse>builder()
                .data(
                        revenueService.getDashboard()
                )
                .build();
    }

    @GetMapping("/monthly")
    public ApiResponse<List<MonthlyRevenueResponse>>
    getMonthlyRevenue(
            @RequestParam Integer year
    ) {

        return ApiResponse
                .<List<MonthlyRevenueResponse>>builder()
                .data(
                        revenueService.getMonthlyRevenue(year)
                )
                .build();
    }
}