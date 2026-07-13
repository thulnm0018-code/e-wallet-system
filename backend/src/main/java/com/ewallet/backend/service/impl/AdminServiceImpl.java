package com.ewallet.backend.service.impl;

import com.ewallet.backend.entity.User;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.dto.response.MonthlyStatisticResponse;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.service.AdminService;

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
}