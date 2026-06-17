package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(UserLoginRequest request);
}