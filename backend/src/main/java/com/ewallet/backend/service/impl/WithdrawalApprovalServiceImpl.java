package com.ewallet.backend.service.impl;

import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.util.TransactionCodeGenerator;
import com.ewallet.backend.dto.response.WithdrawalRequestResponse;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.entity.WithdrawalRequest;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.WithdrawalStatus;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.repository.WithdrawalRequestRepository;
import com.ewallet.backend.service.NotificationService;
import com.ewallet.backend.service.WithdrawalApprovalService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WithdrawalApprovalServiceImpl
        implements WithdrawalApprovalService {

    private final WithdrawalRequestRepository requestRepository;

    private final WalletRepository walletRepository;

    private final NotificationService notificationService;

    private final TransactionRepository transactionRepository;

    private final TransactionCodeGenerator codeGenerator;

    public WithdrawalApprovalServiceImpl(
            WithdrawalRequestRepository requestRepository,
            WalletRepository walletRepository,
            NotificationService notificationService,
            TransactionRepository transactionRepository,
            TransactionCodeGenerator codeGenerator
    ) {
        this.requestRepository = requestRepository;
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
        this.transactionRepository = transactionRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public List<WithdrawalRequestResponse>
    getAllRequests() {

        return requestRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(request ->
                        WithdrawalRequestResponse.builder()
                                .id(request.getId())
                                .userId(request.getUser().getId())
                                .userName(request.getUser().getName())
                                .amount(request.getAmount())
                                .status(request.getStatus().name())
                                .createdAt(request.getCreatedAt())
                                .build()
                )
                .toList();
    }

    @SuppressWarnings("null")
    @Override
    public void approve(Long requestId) {

        WithdrawalRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Withdrawal request not found"
                                ));

        if (request.getStatus()
                != WithdrawalStatus.PENDING) {

            throw new BadRequestException(
                    "Request already processed"
            );
        }

        Wallet wallet =
                walletRepository
                        .findByUser_Id(
                                request.getUser().getId()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Wallet not found"
                                ));

        if (wallet.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new BadRequestException(
                    "Insufficient balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance()
                        .subtract(request.getAmount())
        );

        walletRepository.save(wallet);

        Transaction transaction =
        Transaction.builder()
                .transactionCode(
                        codeGenerator.generate()
                )
                .senderWallet(wallet)
                .receiverWallet(null)
                .amount(request.getAmount())
                .serviceFee(BigDecimal.ZERO)
                .message(
                        "Approved withdrawal"
                )
                .type(
                        TransactionType.WITHDRAW
                )
                .status(
                        TransactionStatus.SUCCESS
                )
                .build();

transactionRepository.save(transaction);

        request.setStatus(
                WithdrawalStatus.APPROVED
        );

        request.setApprovedAt(
                LocalDateTime.now()
        );

        requestRepository.save(request);

        notificationService.createNotification(
                request.getUser(),
                "Withdrawal Approved",
                "Your withdrawal request has been approved"
        );
    }

    @Override
    @SuppressWarnings("null")   
    public void reject(Long requestId) {

        WithdrawalRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Withdrawal request not found"
                                ));

        if (request.getStatus()
                != WithdrawalStatus.PENDING) {

            throw new BadRequestException(
                    "Request already processed"
            );
        }

        request.setStatus(
                WithdrawalStatus.REJECTED
        );

        request.setRejectedAt(
                LocalDateTime.now()
        );

        requestRepository.save(request);

        notificationService.createNotification(
                request.getUser(),
                "Withdrawal Rejected",
                "Your withdrawal request has been rejected"
        );
    }
}
