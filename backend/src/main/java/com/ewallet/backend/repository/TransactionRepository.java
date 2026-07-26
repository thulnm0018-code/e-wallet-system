package com.ewallet.backend.repository;

import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.repository.projection.MonthlyStatisticProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

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

    @Query(value = """
        SELECT t
        FROM Transaction t
        LEFT JOIN FETCH t.senderWallet sw
        LEFT JOIN FETCH sw.user
        LEFT JOIN FETCH t.receiverWallet rw
        LEFT JOIN FETCH rw.user
    """,
    countQuery = "SELECT COUNT(t) FROM Transaction t")
    Page<Transaction> findAllWithUsers(Pageable pageable);

    @Query(value = """
        SELECT t
        FROM Transaction t
        LEFT JOIN FETCH t.senderWallet sw
        LEFT JOIN FETCH sw.user
        LEFT JOIN FETCH t.receiverWallet rw
        LEFT JOIN FETCH rw.user
        WHERE (:status IS NULL OR t.status = :status)
    """,
    countQuery = "SELECT COUNT(t) FROM Transaction t WHERE (:status IS NULL OR t.status = :status)")
    Page<Transaction> findAllWithUsersAndStatus(@Param("status") TransactionStatus status, Pageable pageable);

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.status = com.ewallet.backend.enums.TransactionStatus.PENDING
        AND t.type = com.ewallet.backend.enums.TransactionType.DEPOSIT_REQUEST
    """)
    Long countPendingDepositRequests();

       @Query(value = """
    SELECT
        YEAR(created_at) AS year,
        MONTH(created_at) AS month,
        COUNT(*) AS transactionCount,
        COALESCE(SUM(amount), 0) AS totalVolume
    FROM transactions
    WHERE status = 'SUCCESS'
    GROUP BY YEAR(created_at), MONTH(created_at)
    ORDER BY YEAR(created_at), MONTH(created_at)
    """,
    nativeQuery = true)
List<MonthlyStatisticProjection> getMonthlyStatistics();

            @Query("""
        SELECT COALESCE(SUM(t.serviceFee), 0)
        FROM Transaction t
        WHERE t.status =
        com.ewallet.backend.enums.TransactionStatus.SUCCESS
        AND t.type =
        com.ewallet.backend.enums.TransactionType.TRANSFER
    """)
    BigDecimal getTotalRevenue();

            @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.status =
        com.ewallet.backend.enums.TransactionStatus.SUCCESS
        AND t.type =
        com.ewallet.backend.enums.TransactionType.TRANSFER
    """)
    Long getTotalCompletedTransactions();
        
        @Query("""
    SELECT COALESCE(SUM(t.serviceFee), 0)
    FROM Transaction t
    WHERE YEAR(t.createdAt)=:year
    AND MONTH(t.createdAt)=:month
    AND t.status =
    com.ewallet.backend.enums.TransactionStatus.SUCCESS
    AND t.type =
    com.ewallet.backend.enums.TransactionType.TRANSFER
""")
BigDecimal getRevenueByMonth(
        Integer year,
        Integer month
);

            @Query("""
        SELECT
        MONTH(t.createdAt),
        COALESCE(SUM(t.serviceFee),0)
        FROM Transaction t
        WHERE YEAR(t.createdAt)=:year
        AND t.status =
        com.ewallet.backend.enums.TransactionStatus.SUCCESS
        AND t.type =
        com.ewallet.backend.enums.TransactionType.TRANSFER
        GROUP BY MONTH(t.createdAt)
        ORDER BY MONTH(t.createdAt)
    """)
    List<Object[]> getMonthlyRevenue(Integer year);

            @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.senderWallet.user.id = :userId
            AND t.type =
                com.ewallet.backend.enums.TransactionType.TRANSFER
            AND t.status =
                com.ewallet.backend.enums.TransactionStatus.SUCCESS
            AND t.createdAt >= :since
        """)
        Long countRecentTransfers(
                @Param("userId") Long userId,
                @Param("since") LocalDateTime since
        );

            @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.senderWallet.user.id = :userId
        AND t.type =
            com.ewallet.backend.enums.TransactionType.TRANSFER
        AND t.status =
            com.ewallet.backend.enums.TransactionStatus.SUCCESS
        AND t.createdAt >= :start
        AND t.createdAt <= :end
    """)
    BigDecimal getTodayTransferAmount(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<Transaction>findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.id = :id
    """)
    Optional<Transaction> findByIdForUpdate(@Param("id") Long id);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.status =
    com.ewallet.backend.enums.TransactionStatus.SUCCESS
""")
BigDecimal getTotalTransactionVolume();
}