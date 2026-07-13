package com.ewallet.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.MonthlyStatisticResponse;
import com.ewallet.backend.enums.UserStatus;

import java.util.List;
public interface AdminService {

    AdminDashboardResponse getDashboard();

    List<MonthlyStatisticResponse> getMonthlyStatistics();

    Page<AdminUserResponse> searchUsers(
        String keyword,
        UserStatus status,
        Pageable pageable
    );
}