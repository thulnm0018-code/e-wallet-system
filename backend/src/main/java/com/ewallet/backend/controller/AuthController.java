package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse loginResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .message("Login successful")
                        .data(loginResponse)
                        .build()
        );
    }
}