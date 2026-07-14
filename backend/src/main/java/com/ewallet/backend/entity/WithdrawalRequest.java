package com.ewallet.backend.entity;

import com.ewallet.backend.enums.WithdrawalStatus;

import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "withdrawal_requests",
        indexes = {
                @Index(
                        name = "idx_withdraw_request_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_withdraw_request_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_withdraw_request_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WithdrawalStatus status;

    
        @Column(
                name = "idempotency_key",
                unique = true,
                length = 100
        )
        private String idempotencyKey;


    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    public boolean isPending() {
        return status == WithdrawalStatus.PENDING;
    }

    public boolean isApproved() {
        return status == WithdrawalStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == WithdrawalStatus.REJECTED;
    }


}