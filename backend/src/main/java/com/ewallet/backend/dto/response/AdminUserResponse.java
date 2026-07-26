package com.ewallet.backend.dto.response;

import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private UserStatus userStatus;
    private BigDecimal balance;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;

    public static AdminUserResponse fromEntity(User user, BigDecimal balance) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .userStatus(user.getUserStatus())
                .balance(balance)
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
