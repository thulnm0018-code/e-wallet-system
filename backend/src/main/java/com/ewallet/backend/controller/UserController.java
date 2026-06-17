package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
   public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse userResponse = userService.registerUser(request);
        
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .message("User registered successfully")
                .data(userResponse)
                .build();
       
                return ResponseEntity.ok(response);
    }
}
