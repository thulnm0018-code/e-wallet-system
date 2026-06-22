package com.ewallet.backend.scheduler;

import com.ewallet.backend.repository.RefreshTokenRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupTask {

    private static final Logger log =
            LoggerFactory.getLogger(TokenCleanupTask.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupTask(
            RefreshTokenRepository refreshTokenRepository) {

        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(
            cron = "0 0 2 * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    @Transactional
    public void cleanupExpiredTokens() {

        long deletedCount =
                refreshTokenRepository.deleteByExpiresAtBefore(
                        LocalDateTime.now());

        log.info(
                "Deleted {} expired refresh tokens",
                deletedCount
        );
    }
}