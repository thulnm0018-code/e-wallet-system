package com.ewallet.backend.service.impl;

import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.PessimisticLockingFailureException;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
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

        validateAmount(request.getAmount());

        String receiverPhone = PhoneUtils.normalize(request.getReceiverPhone());
        if (receiverPhone == null) {
            throw new BadRequestException("Invalid receiver phone number");
        }

        Wallet senderWalletTemp = getWalletByUserId(senderUserId);

        Wallet receiverWalletTemp = walletRepository
                .findByUser_Phone(receiverPhone)
                .orElseThrow(() -> new NotFoundException("Receiver wallet not found"));

        if (senderWalletTemp.getId().equals(receiverWalletTemp.getId())) {
            throw new BadRequestException("Cannot transfer money to yourself");
        }

        Long firstWalletId = Math.min(senderWalletTemp.getId(), receiverWalletTemp.getId());
        Long secondWalletId = Math.max(senderWalletTemp.getId(), receiverWalletTemp.getId());

        Wallet firstLockedWallet = lockWallet(firstWalletId);
        Wallet secondLockedWallet = lockWallet(secondWalletId);

        Wallet senderWallet = firstLockedWallet.getId().equals(senderWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        Wallet receiverWallet = firstLockedWallet.getId().equals(receiverWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        
        validateActiveWallet(senderWallet, "Sender wallet");
        validateActiveWallet(receiverWallet, "Receiver wallet");

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
    @Transactional
    public TransactionResponse depositMoney(DepositRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        validateAmount(request.getAmount());

        Wallet wallet = getWalletByUserId(userId);
        Wallet lockedWallet = lockWallet(wallet.getId());

    
        validateActiveWallet(lockedWallet, "Wallet");


    lockedWallet.setBalance(lockedWallet.getBalance()
                .add(request.getAmount()));

    walletRepository.save(lockedWallet);

    Transaction transaction = Transaction.builder()
            .transactionCode(codeGenerator.generate())
            .senderWallet(null)
            .receiverWallet(lockedWallet)
            .amount(request.getAmount())
            .message(request.getMessage() == null
                        || request.getMessage().isBlank()
                        ? "Deposit money"
                        : request.getMessage())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();

    Transaction savedTransaction = transactionRepository.save(transaction);

    return transactionMapper.toResponse(savedTransaction);
}
    
    @Override
    @Transactional
    public TransactionResponse withdrawMoney(WithdrawRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        validateAmount(request.getAmount());

        Wallet wallet = getWalletByUserId(userId);

      Wallet lockedWallet = lockWallet(wallet.getId());

        validateActiveWallet(lockedWallet, "Wallet");


    if (lockedWallet.getBalance()
            .compareTo(request.getAmount()) < 0) {

        throw new BadRequestException(
                "Insufficient balance"
        );
    }

    lockedWallet.setBalance(lockedWallet.getBalance()
                .subtract(request.getAmount()));

    walletRepository.save(lockedWallet);

    Transaction transaction = Transaction.builder()
            .transactionCode(
                    codeGenerator.generate()
            )

            .senderWallet(lockedWallet)

            .receiverWallet(null)

            .amount(request.getAmount())

            .message(
                    request.getMessage() == null
                            || request.getMessage().isBlank()
                            ? "Withdraw money"
                            : request.getMessage()
            )

            .type(TransactionType.WITHDRAW)
            .status(TransactionStatus.SUCCESS)
            .build();

    Transaction savedTransaction =
            transactionRepository.save(transaction);

    return transactionMapper.toResponse(
            savedTransaction
    );
}

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory() {
       Long userId = currentUserService.getCurrentUserId();
       
        Wallet wallet = getWalletByUserId(userId);

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

       Wallet wallet = getWalletByUserId(userId);

        return wallet.getBalance();
    }

        private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException(
                    "Amount must be greater than zero"
            );
        }
   }
        private Wallet getWalletByUserId(Long userId) {
                return walletRepository.findByUser_Id(userId)
                        .orElseThrow(() -> new NotFoundException("Wallet not found"));
        }
        
        
        private void validateActiveWallet(Wallet wallet, String walletName) {

        if (wallet.getWalletStatus() != WalletStatus.ACTIVE) {
                throw new BadRequestException(walletName + " is inactive");}
        }
         
private Wallet lockWallet(Long walletId) {

        try {return walletRepository
                        .findByIdForUpdate(walletId)
                        .orElseThrow(() ->
                new NotFoundException("Wallet not found"));
        } 

        catch (PessimisticLockingFailureException | LockTimeoutException ex) {

                throw new ResourceConflictException(
                        "Wallet is currently being processed. Please try again later.");}
        }

}