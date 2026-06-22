package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private final long expiresIn; // seconds
    private final UserResponse user;
}
