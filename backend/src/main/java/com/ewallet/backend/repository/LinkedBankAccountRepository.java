package com.ewallet.backend.repository;

import com.ewallet.backend.entity.LinkedBankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkedBankAccountRepository
        extends JpaRepository<LinkedBankAccount, Long> {

    List<LinkedBankAccount> findByUser_Id(Long userId);

    Optional<LinkedBankAccount> findByIdAndUser_Id(
            Long accountId,
            Long userId
    );

    boolean existsByUser_IdAndAccountNumber(
            Long userId,
            String accountNumber
    );
}