package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.ReceiverLookupResponse;
import com.ewallet.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<ReceiverLookupResponse>> getUserByPhone(@PathVariable String phone) {
        ReceiverLookupResponse response = userService.getUserByPhone(phone);
        return ResponseEntity.ok(
                ApiResponse.<ReceiverLookupResponse>builder()
                        .message("User found")
                        .data(response)
                        .build()
        );
    }
}