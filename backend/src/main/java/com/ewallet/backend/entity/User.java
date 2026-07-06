package com.ewallet.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import com.ewallet.backend.enums.UserStatus;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable =false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;


    @Column(nullable = false, length = 255)
    private String passwordHash; // luu password da duoc hash


    public enum Role {
        USER, ADMIN
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 50)
    private UserStatus userStatus = UserStatus.PENDING_VERIFICATION;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Otp> otps = new ArrayList<>();

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
