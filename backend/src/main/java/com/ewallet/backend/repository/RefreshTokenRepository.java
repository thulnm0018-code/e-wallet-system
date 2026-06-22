package com.ewallet.backend.repository;

import com.ewallet.backend.entity.RefreshToken;
import com.ewallet.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    long deleteByExpiresAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("""
        update RefreshToken rt
        set rt.revoked = true
        where rt.user = :user
          and rt.revoked = false
    """)
    void revokeAllByUser(@Param("user") User user);

}