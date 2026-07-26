package com.ewallet.backend.service.impl;

import com.ewallet.backend.service.RabbitProducerService;
import com.ewallet.backend.service.AuditLogService;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.dto.response.MonthlyStatisticResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.service.AdminService;
import com.ewallet.backend.util.TransactionCodeGenerator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCodeGenerator codeGenerator;
    private final RabbitProducerService rabbitProducerService;
    private final AuditLogService auditLogService;

    public AdminServiceImpl(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            TransactionCodeGenerator codeGenerator,
            RabbitProducerService rabbitProducerService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.codeGenerator = codeGenerator;
        this.rabbitProducerService = rabbitProducerService;
        this.auditLogService = auditLogService;
    }

    @Override
    public AdminDashboardResponse getDashboard() {

        BigDecimal totalVolume =
                transactionRepository.getTotalTransactionVolume();

        if (totalVolume == null) {
            totalVolume = BigDecimal.ZERO;
        }

        return AdminDashboardResponse.builder()
                .totalUsers(
                        userRepository.countByDeletedFalse()
                )
                .activeUsers(
                        userRepository.countByUserStatusAndDeletedFalse(
                                UserStatus.ACTIVE
                        )
                )
                .lockedUsers(
                        userRepository.countByUserStatusAndDeletedFalse(
                                UserStatus.LOCKED
                        )
                )
                .activeWallets(
                        walletRepository.countByWalletStatus(
                                WalletStatus.ACTIVE
                        )
                )
                .totalTransactions(
                        transactionRepository.count()
                )
                .totalVolume(
                        totalVolume
                )
                .totalRevenue(
                        BigDecimal.ZERO
                )
                .pendingReviews(
                        transactionRepository.countPendingDepositRequests()
                )
                .build();
    }
    @Override
public List<MonthlyStatisticResponse> getMonthlyStatistics() {

    return transactionRepository
            .getMonthlyStatistics()
            .stream()
            .map(item ->
                    new MonthlyStatisticResponse(
                            item.getYear() + "-"
                                    + String.format("%02d", item.getMonth()),
                            item.getTransactionCount(),
                            item.getTotalVolume()
                    )
            )
            .toList();
}

@SuppressWarnings("null")
@Override
public Page<AdminUserResponse> searchUsers(
        String keyword,
        UserStatus status,
        Pageable pageable
) {

    return userRepository
            .searchUsers(
                    keyword,
                    status,
                    pageable
            )
            .map(user ->
                    AdminUserResponse.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .role(user.getRole().name())
                            .userStatus(user.getUserStatus())
                            .address(user.getAddress())
                            .dateOfBirth(user.getDateOfBirth())
                                .createdAt(user.getCreatedAt())
                                .balance(
                                        walletRepository
                                                .findByUser_Id(user.getId())
                                                .map(Wallet::getBalance)
                                                .orElse(BigDecimal.ZERO)
                                )
                            .build()
            );
}

@Override
@Transactional
public void deleteUser(Long userId) {

    Long currentUserId =
        Long.parseLong(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
                        .toString()
        );

if (currentUserId.equals(userId)) {
    throw new BadRequestException("Cannot delete your own account");
}

    User user = userRepository.findByIdAndDeletedFalse(userId)
            .orElseThrow(() ->new NotFoundException("User not found"));

            user.setDeleted(true);
            userRepository.save(user);
}

     @Override
    @Transactional
public void restoreUser(Long userId) {

    User user = userRepository
            .findByIdAndDeletedTrue(userId)
            .orElseThrow(() ->
                    new NotFoundException("Deleted user not found"));

    user.setDeleted(false);

    userRepository.save(user);
}   

    // Admin direct deposit endpoint removed. Use approval flow for deposit requests instead.

    @SuppressWarnings("null")
    @Override
    @Transactional
    public void approveDepositRequest(Long transactionId) {
        Transaction requestTransaction = transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new NotFoundException("Deposit request not found"));

        if (requestTransaction.getType() != com.ewallet.backend.enums.TransactionType.DEPOSIT_REQUEST) {
            throw new BadRequestException("Only deposit requests can be approved");
        }

        if (requestTransaction.getStatus() != com.ewallet.backend.enums.TransactionStatus.PENDING) {
            throw new BadRequestException("Deposit request is not pending");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(requestTransaction.getReceiverWallet().getId())
                .orElseThrow(() -> new NotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(requestTransaction.getAmount()));
        walletRepository.save(wallet);

        User admin = userRepository.findById(Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        )).orElseThrow(() -> new NotFoundException("Admin user not found"));

        Transaction depositTransaction = Transaction.builder()
                .transactionCode(codeGenerator.generate())
                .senderWallet(null)
                .receiverWallet(wallet)
                .amount(requestTransaction.getAmount())
                .serviceFee(java.math.BigDecimal.ZERO)
                .message("Deposit approved")
                .type(com.ewallet.backend.enums.TransactionType.DEPOSIT)
                .status(com.ewallet.backend.enums.TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(depositTransaction);

        requestTransaction.setStatus(com.ewallet.backend.enums.TransactionStatus.APPROVED);
        requestTransaction.setApprovedBy(admin.getId());
        requestTransaction.setApprovedAt(java.time.LocalDateTime.now());
        transactionRepository.save(requestTransaction);

        auditLogService.log(admin, com.ewallet.backend.enums.AuditAction.DEPOSIT, "Approved deposit request " + requestTransaction.getTransactionCode());

        rabbitProducerService.sendNotification(com.ewallet.backend.dto.message.NotificationMessage.builder()
                .userId(wallet.getUser().getId())
                .title("Deposit Approved")
                .content("Your deposit request of " + requestTransaction.getAmount() + " VND has been approved.")
                .build());
    }

  
        @Override
    @Transactional
    public void rejectDepositRequest(Long transactionId) {
        Transaction requestTransaction = transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new NotFoundException("Deposit request not found"));

        if (requestTransaction.getType() != com.ewallet.backend.enums.TransactionType.DEPOSIT_REQUEST) {
            throw new BadRequestException("Only deposit requests can be rejected");
        }

        if (requestTransaction.getStatus() != com.ewallet.backend.enums.TransactionStatus.PENDING) {
            throw new BadRequestException("Deposit request is not pending");
        }

        User admin = userRepository.findById(Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
        )).orElseThrow(() -> new NotFoundException("Admin user not found"));

        requestTransaction.setStatus(com.ewallet.backend.enums.TransactionStatus.REJECTED);
        requestTransaction.setApprovedBy(admin.getId());
        requestTransaction.setApprovedAt(java.time.LocalDateTime.now());
        transactionRepository.save(requestTransaction);

        auditLogService.log(admin, com.ewallet.backend.enums.AuditAction.DEPOSIT, "Rejected deposit request " + requestTransaction.getTransactionCode());

        rabbitProducerService.sendNotification(com.ewallet.backend.dto.message.NotificationMessage.builder()
                .userId(requestTransaction.getReceiverWallet().getUser().getId())
                .title("Deposit Rejected")
                .content("Your deposit request of " + requestTransaction.getAmount() + " VND has been rejected.")
                .build());
    }
}