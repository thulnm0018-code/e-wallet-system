package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.exception.UnauthorizedException;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.AuthService;
import com.ewallet.backend.util.PhoneUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        String input = request.getIdentifier().trim();
        User user;

        boolean isEmail = input.matches("^[A-Za-z0-9+_.-]+@(.+)$");

        if (isEmail) {
            user = userRepository.findByEmail(input.toLowerCase())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        } else {
           String cleanPhone = PhoneUtils.normalize(input);

            if (cleanPhone.length() < 9 || cleanPhone.length() > 11) {
                throw new UnauthorizedException("Invalid credentials");
            }

            user = userRepository.findByPhone(cleanPhone)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        UserResponse userResponse = UserResponse.fromEntity(user);

        return LoginResponse.builder()
                .accessToken("mock-access-token-jwt-secret-xyz")
                .refreshToken("mock-refresh-token-jwt-secret-abc")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userResponse)
                .build();
    }
}