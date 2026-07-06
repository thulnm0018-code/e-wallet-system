package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.TransferInitiateRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.dto.response.TransferOtpResponse;
import com.ewallet.backend.dto.response.WalletResponse;


import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    TransferOtpResponse initiateTransfer(TransferInitiateRequest request);
    TransactionResponse transferMoney(TransferRequest request);
    List<TransactionResponse> getMyHistory();
    BigDecimal getMyBalance();
    
    TransactionResponse depositMoney(DepositRequest request);
    TransactionResponse withdrawMoney(WithdrawRequest request);
    WalletResponse getMyWallet();
    List<TransactionResponse> getMyHistory(String type, String startDate, String endDate);
    List<TransactionResponse> getMyHistory(String type, String startDate, String endDate, Integer page, Integer size);
}