package com.ewallet.backend.repository;

import com.ewallet.backend.entity.Wallet;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser_Id(Long userId);

    Optional<Wallet> findByUser_Phone(String phone);

    Optional<Wallet> findByUser_Email(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w
        FROM Wallet w
        WHERE w.id = :walletId
    """)
    Optional<Wallet> findByIdForUpdate(
        @Param("walletId") Long walletId
    );
}