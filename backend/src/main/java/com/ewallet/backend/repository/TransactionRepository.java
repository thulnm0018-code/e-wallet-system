package com.ewallet.backend.repository;

import com.ewallet.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    WHERE t.senderWallet.id = :walletId 
       OR t.receiverWallet.id = :walletId 
    ORDER BY t.createdAt DESC
    """)
    List<Transaction> findWalletTransactions(@Param("walletId") Long walletId);
}