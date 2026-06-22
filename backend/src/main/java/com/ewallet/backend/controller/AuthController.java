package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
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
}
