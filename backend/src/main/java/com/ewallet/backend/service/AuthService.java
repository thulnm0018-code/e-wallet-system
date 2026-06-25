package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse login(UserLoginRequest request, HttpServletResponse response);
    LoginResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
    UserResponse getCurrentUser();

    void logout(HttpServletRequest request, HttpServletResponse response); 
    void verifyOtp(String phoneOrEmail, String otpCode);
}