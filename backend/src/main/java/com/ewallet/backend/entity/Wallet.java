package com.ewallet.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "wallets")
@Getter
@Setter

public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;


    public enum WalletStatus {
        ACTIVE, FROZEN, CLOSED
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus walletStatus = WalletStatus.ACTIVE;
    
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

     @PrePersist
    protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

    @PreUpdate
    protected void onUpdate() {
    updatedAt = LocalDateTime.now();
}
}
