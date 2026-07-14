package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.response.SuspiciousActivityResponse;
import com.ewallet.backend.entity.SuspiciousActivity;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.SuspiciousActivityRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.service.SuspiciousActivityService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class SuspiciousActivityServiceImpl
        implements SuspiciousActivityService {

    private static final long RAPID_TRANSFER_THRESHOLD = 5;

    private static final BigDecimal DAILY_TRANSFER_LIMIT =
            new BigDecimal("10000");

    private final TransactionRepository transactionRepository;

    private final SuspiciousActivityRepository
            suspiciousActivityRepository;

    public SuspiciousActivityServiceImpl(
            TransactionRepository transactionRepository,
            SuspiciousActivityRepository suspiciousActivityRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.suspiciousActivityRepository =
                suspiciousActivityRepository;
    }

    @Override
    public void detectRapidTransfers(User user) {

        Long count =
                transactionRepository.countRecentTransfers(
                        user.getId(),
                        LocalDateTime.now().minusMinutes(1)
                );

        if (count >= RAPID_TRANSFER_THRESHOLD) {

            createActivity(
                    user,
                    "Rapid Transfer Detection",
                    "User performed "
                            + count
                            + " transfers within 1 minute"
            );
        }
    }

    @Override
    public void detectDailyTransferLimit(User user) {

        LocalDate today = LocalDate.now();

        BigDecimal total =
                transactionRepository.getTodayTransferAmount(
                        user.getId(),
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                );

        if (total.compareTo(
                DAILY_TRANSFER_LIMIT) > 0) {

            createActivity(
                    user,
                    "Daily Transfer Limit Exceeded",
                    "Total transfer amount today: "
                            + total
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspiciousActivityResponse>
    getAllActivities() {

        return suspiciousActivityRepository
                .findAllByOrderByDetectedAtDesc()
                .stream()
                .map(activity ->
                        SuspiciousActivityResponse
                                .builder()
                                .id(activity.getId())
                                .userId(
                                        activity.getUser().getId()
                                )
                                .userName(
                                        activity.getUser().getName()
                                )
                                .reason(
                                        activity.getReason()
                                )
                                .details(
                                        activity.getDetails()
                                )
                                .detectedAt(
                                        activity.getDetectedAt()
                                )
                                .build()
                )
                .toList();
    }

    @SuppressWarnings("null")
    private void createActivity(
            User user,
            String reason,
            String details
    ) {

        SuspiciousActivity activity =
                SuspiciousActivity.builder()
                        .user(user)
                        .reason(reason)
                        .details(details)
                        .detectedAt(
                                LocalDateTime.now()
                        )
                        .build();

        suspiciousActivityRepository.save(
                activity
        );
    }
}