package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.LinkBankAccountRequest;
import com.ewallet.backend.dto.response.LinkedBankAccountResponse;

import java.util.List;

public interface LinkedBankAccountService {

    void linkBankAccount(
            LinkBankAccountRequest request
    );

    List<LinkedBankAccountResponse>
    getMyLinkedAccounts();

    void unlinkAccount(Long accountId);
}