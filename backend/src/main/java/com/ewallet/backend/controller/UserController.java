package com.ewallet.backend.controller;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreateRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }
}