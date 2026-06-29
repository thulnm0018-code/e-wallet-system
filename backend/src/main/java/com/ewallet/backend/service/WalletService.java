package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
import com.ewallet.backend.dto.response.TransactionResponse;


import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    TransactionResponse transferMoney(TransferRequest request);
    List<TransactionResponse> getMyHistory();
    BigDecimal getMyBalance();
    
    TransactionResponse depositMoney(DepositRequest request);
    TransactionResponse withdrawMoney(WithdrawRequest request);
}