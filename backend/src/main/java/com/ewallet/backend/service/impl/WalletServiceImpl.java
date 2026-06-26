package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.service.WalletService;
import com.ewallet.backend.util.PhoneUtils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse transferMoney(TransferRequest request) {
        Long senderUserId = getCurrentUserId();

        String receiverPhone = PhoneUtils.normalize(request.getReceiverPhone());
        if (receiverPhone == null) {
            throw new RuntimeException("Invalid receiver phone number");
        }

        Wallet senderWalletTemp = walletRepository
                .findByUser_Id(senderUserId)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        Wallet receiverWalletTemp = walletRepository
                .findByUser_Phone(receiverPhone)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        if (senderWalletTemp.getId().equals(receiverWalletTemp.getId())) {
            throw new RuntimeException("Cannot transfer money to yourself");
        }

        Long firstWalletId = Math.min(senderWalletTemp.getId(), receiverWalletTemp.getId());
        Long secondWalletId = Math.max(senderWalletTemp.getId(), receiverWalletTemp.getId());

        Wallet firstLockedWallet = walletRepository
                .findByIdForUpdate(firstWalletId)
                .orElseThrow(() -> new RuntimeException("Could not acquire wallet lock"));

        Wallet secondLockedWallet = walletRepository
                .findByIdForUpdate(secondWalletId)
                .orElseThrow(() -> new RuntimeException("Could not acquire wallet lock"));

        Wallet senderWallet = firstLockedWallet.getId().equals(senderWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        Wallet receiverWallet = firstLockedWallet.getId().equals(receiverWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        if (senderWallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Sender wallet is inactive");
        }

        if (receiverWallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new RuntimeException("Receiver wallet is inactive");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));

        walletRepository.saveAll(List.of(senderWallet, receiverWallet));

        Transaction transaction = Transaction.builder()
                .transactionCode(generateTransactionCode())
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
        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory() {
        Long userId = getCurrentUserId();

        Wallet wallet = walletRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return transactionRepository
                .findWalletTransactions(wallet.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyBalance() {
        Long userId = getCurrentUserId();

        Wallet wallet = walletRepository
                .findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        return wallet.getBalance();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return Long.parseLong(principal.toString());
    }

    private String generateTransactionCode() {
        return "TX" + System.currentTimeMillis() + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .transactionCode(tx.getTransactionCode())
                .senderPhone(tx.getSenderWallet() != null 
                        ? tx.getSenderWallet().getUser().getPhone() 
                        : "SYSTEM")
                .receiverPhone(tx.getReceiverWallet() != null 
                        ? tx.getReceiverWallet().getUser().getPhone() 
                        : "SYSTEM")
                .amount(tx.getAmount())
                .message(tx.getMessage())
                .status(tx.getStatus())
                .type(tx.getType())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}