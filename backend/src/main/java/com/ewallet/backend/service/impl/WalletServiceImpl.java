package com.ewallet.backend.service.impl;

import org.springframework.dao.PessimisticLockingFailureException;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.CurrentUserService;
import com.ewallet.backend.service.WalletService;
import com.ewallet.backend.util.PhoneUtils;
import com.ewallet.backend.util.TransactionCodeGenerator;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.mapper.TransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCodeGenerator codeGenerator;
    private final CurrentUserService currentUserService;
    private final TransactionMapper transactionMapper;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            TransactionCodeGenerator codeGenerator,
            CurrentUserService currentUserService,
            TransactionMapper transactionMapper) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.codeGenerator = codeGenerator;
        this.currentUserService = currentUserService;
        this.transactionMapper = transactionMapper;
    }

    @Override
    @Transactional
    public TransactionResponse transferMoney(TransferRequest request) {
        Long senderUserId = currentUserService.getCurrentUserId();

            if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        String receiverPhone = PhoneUtils.normalize(request.getReceiverPhone());
        if (receiverPhone == null) {
            throw new BadRequestException("Invalid receiver phone number");
        }

        Wallet senderWalletTemp = walletRepository
                .findByUser_Id(senderUserId)
                .orElseThrow(() -> new NotFoundException("Sender wallet not found"));

        Wallet receiverWalletTemp = walletRepository
                .findByUser_Phone(receiverPhone)
                .orElseThrow(() -> new NotFoundException("Receiver wallet not found"));

        if (senderWalletTemp.getId().equals(receiverWalletTemp.getId())) {
            throw new BadRequestException("Cannot transfer money to yourself");
        }

        Long firstWalletId = Math.min(senderWalletTemp.getId(), receiverWalletTemp.getId());
        Long secondWalletId = Math.max(senderWalletTemp.getId(), receiverWalletTemp.getId());

        Wallet firstLockedWallet;
        Wallet secondLockedWallet;

        try {
            firstLockedWallet = walletRepository
                    .findByIdForUpdate(firstWalletId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Wallet not found during lock acquisition"
                    ));

            secondLockedWallet = walletRepository
                    .findByIdForUpdate(secondWalletId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Wallet not found during lock acquisition"
                    ));

        } catch (PessimisticLockingFailureException ex) {
            throw new ResourceConflictException(
                    "Wallet is currently being processed. Please try again later."
            );
        }

        Wallet senderWallet = firstLockedWallet.getId().equals(senderWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        Wallet receiverWallet = firstLockedWallet.getId().equals(receiverWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        if (senderWallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new BadRequestException("Sender wallet is inactive");
        }

        if (receiverWallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new BadRequestException("Receiver wallet is inactive");
        }

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));

        walletRepository.saveAll(List.of(senderWallet, receiverWallet));

        Transaction transaction = Transaction.builder()
                
                .transactionCode(codeGenerator.generate())
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(request.getAmount())
                .message(request.getMessage() == null || request.getMessage().isBlank() 
                        ? "Transfer" 
                        : request.getMessage())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory() {
       Long userId = currentUserService.getCurrentUserId();

        Wallet wallet = walletRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        return transactionRepository
                .findWalletTransactions(wallet.getId())
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyBalance() {
        Long userId = currentUserService.getCurrentUserId();

        Wallet wallet = walletRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        return wallet.getBalance();
    }

 
}