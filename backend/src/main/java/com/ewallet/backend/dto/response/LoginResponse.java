package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;

    private final String tokenType; // "Bearer"

    private final long expiresIn; // seconds

    private final UserResponse user;
}
