package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Registering user with email={}", request.getEmail());

        userService.registerUser(request);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
}
