package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.ForgotPasswordRequest;
import com.ewallet.backend.dto.request.ResetPasswordRequest;
import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.request.VerifyOtpRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.service.AuthService;
import com.ewallet.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse userResponse = userService.registerUser(request);
        return new ResponseEntity<>(
                ApiResponse.<UserResponse>builder()
                        .message("Registration successful. Please verify OTP.")
                        .data(userResponse)
                        .build(),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getIdentifier(), request.getOtpCode());
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Account activated and e-wallet created successfully!")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request, response);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .message("Login successful")
                        .data(loginResponse)
                        .build()
        );
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.refreshToken(request, response);
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .message("Refresh successful")
                        .data(loginResponse)
                        .build()
        );
    }

    @GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {

    UserResponse userResponse = authService.getCurrentUser();

    return ResponseEntity.ok(
            ApiResponse.<UserResponse>builder()
                    .message("Fetch user session successful")
                    .data(userResponse)
                    .build()
    );
}
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Logged out successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("If the account exists, a reset OTP has been generated")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Password reset successfully")
                        .data(null)
                        .build()
        );
    }
}
