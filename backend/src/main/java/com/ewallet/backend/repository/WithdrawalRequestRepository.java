package com.ewallet.backend.repository;

import com.ewallet.backend.entity.WithdrawalRequest;
import com.ewallet.backend.enums.WithdrawalStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRequestRepository
        extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest>
    findAllByOrderByCreatedAtDesc();

    List<WithdrawalRequest>
    findByStatusOrderByCreatedAtDesc(
            WithdrawalStatus status
    );

    Optional<WithdrawalRequest> findByIdempotencyKey(
        String idempotencyKey
);
}