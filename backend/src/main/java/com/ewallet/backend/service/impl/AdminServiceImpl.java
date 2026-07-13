package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public AdminServiceImpl(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
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
                        0L
                )
                .build();
    }
}