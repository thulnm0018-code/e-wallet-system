package com.ewallet.backend.dto.response;

import com.ewallet.backend.entity.User;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String userStatus;
    private String avatarUrl;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {  // Convert User entity to UserResponse DTO
        if (user == null) return null;
        
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .userStatus(user.getUserStatus() != null ? user.getUserStatus().name() : null)
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .build();
    }
}