package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.SuspiciousActivityResponse;
import com.ewallet.backend.entity.User;

import java.util.List;

public interface SuspiciousActivityService {

    void detectRapidTransfers(User user);

    void detectDailyTransferLimit(User user);

    List<SuspiciousActivityResponse> getAllActivities();
}