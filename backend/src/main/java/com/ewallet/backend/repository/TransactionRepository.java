package com.ewallet.backend.repository;

import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.repository.projection.MonthlyStatisticProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByTransactionCode(String transactionCode);

    Optional<Transaction> findByTransactionCode(String transactionCode);

    List<Transaction>
    findBySenderWallet_IdOrderByCreatedAtDesc(
        Long walletId
    );

    List<Transaction>
    findByReceiverWallet_IdOrderByCreatedAtDesc(
        Long walletId
    );

        @Query("""
    SELECT t 
    FROM Transaction t 
    LEFT JOIN FETCH t.senderWallet sw 
    LEFT JOIN FETCH sw.user su 
    LEFT JOIN FETCH t.receiverWallet rw 
    LEFT JOIN FETCH rw.user ru 
    WHERE (t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId)
      AND (:type IS NULL OR t.type = :type)
      AND (:startDate IS NULL OR t.createdAt >= :startDate)
      AND (:endDate IS NULL OR t.createdAt <= :endDate)
    ORDER BY t.createdAt DESC
    """)
    List<Transaction> findWalletTransactions(@Param("walletId") Long walletId,
                                             @Param("type") TransactionType type,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("""
    SELECT t 
    FROM Transaction t 
    LEFT JOIN FETCH t.senderWallet sw 
    LEFT JOIN FETCH sw.user su 
    LEFT JOIN FETCH t.receiverWallet rw 
    LEFT JOIN FETCH rw.user ru 
    WHERE (t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId)
      AND (:type IS NULL OR t.type = :type)
      AND (:startDate IS NULL OR t.createdAt >= :startDate)
      AND (:endDate IS NULL OR t.createdAt <= :endDate)
    ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findWalletTransactions(@Param("walletId") Long walletId,
                                             @Param("type") TransactionType type,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    @Query("""
        SELECT t
        FROM Transaction t
        LEFT JOIN FETCH t.senderWallet sw
        LEFT JOIN FETCH sw.user
        LEFT JOIN FETCH t.receiverWallet rw
        LEFT JOIN FETCH rw.user
        ORDER BY t.createdAt DESC
    """)
    List<Transaction> findAllForExport();

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.status = com.ewallet.backend.enums.TransactionStatus.SUCCESS
    """)
    BigDecimal getTotalTransactionVolume();

    @Query(value = """
    SELECT
        YEAR(created_at) AS year,
        MONTH(created_at) AS month,
        COUNT(*) AS transactionCount,
        COALESCE(SUM(amount),0) AS totalVolume
    FROM transactions
    WHERE status = 'SUCCESS'
    GROUP BY YEAR(created_at), MONTH(created_at)
    ORDER BY YEAR(created_at), MONTH(created_at)
    """,
    nativeQuery = true)
    List<MonthlyStatisticProjection> getMonthlyStatistics();
}