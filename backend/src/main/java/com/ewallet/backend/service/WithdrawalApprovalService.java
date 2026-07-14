package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.WithdrawalRequestResponse;

import java.util.List;

public interface WithdrawalApprovalService {

    List<WithdrawalRequestResponse>
    getAllRequests();

    void approve(Long requestId);

    void reject(Long requestId);
}