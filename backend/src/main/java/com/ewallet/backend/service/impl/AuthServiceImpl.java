package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.entity.RefreshToken;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.exception.UnauthorizedException;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.RefreshTokenRepository;
import com.ewallet.backend.service.AuthService;
import com.ewallet.backend.security.JwtTokenProvider;
import com.ewallet.backend.util.CookieUtils;
import com.ewallet.backend.util.PhoneUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, 
                            RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder, 
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public LoginResponse login(UserLoginRequest request, HttpServletResponse response) {
        String input = request.getIdentifier().trim();
        User user;

        boolean isEmail = input.matches("^[A-Za-z0-9+_.-]+@(.+)$");

        if (isEmail) {
            user = userRepository.findByEmail(input.toLowerCase())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        } else {
           String cleanPhone = PhoneUtils.normalize(input);

           if (cleanPhone == null) {
                throw new UnauthorizedException("Invalid credentials");
            }

            user = userRepository.findByPhone(cleanPhone)
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

         return createLoginSession(user, response);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = getRefreshTokenFromRequest(request);

        if (refreshTokenValue == null || !jwtTokenProvider.isValidRefreshToken(refreshTokenValue)) {
            throw new UnauthorizedException("Refresh token is invalid");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (storedToken.getRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken newRefreshToken =
                    RefreshToken.builder()
                            .token(newRefreshTokenValue)
                            .user(user)
                            .expiresAt(LocalDateTime.now().plusDays(7))
                            .build();

            refreshTokenRepository.save(newRefreshToken);


        CookieUtils.createCookie(response, "accessToken", newAccessToken, 900);
        CookieUtils.createCookie(response, "refreshToken", newRefreshTokenValue, 604800);

        return LoginResponse.builder()
                .expiresIn(900)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
            || !authentication.isAuthenticated() 
            || "anonymousUser".equals(authentication.getPrincipal())) {
        throw new UnauthorizedException("Not authenticated");
    }

    Long userId = Long.parseLong(authentication.getPrincipal().toString());

    User user = userRepository.findById(userId)
            .orElseThrow(() ->
                    new UnauthorizedException("User not found"));

    return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String refreshTokenValue = getRefreshTokenFromRequest(request);

        if (refreshTokenValue != null) {
            refreshTokenRepository.findByToken(refreshTokenValue)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }

        CookieUtils.clearCookie(response, "accessToken");
        CookieUtils.clearCookie(response, "refreshToken");

        SecurityContextHolder.clearContext();
    }

    private LoginResponse createLoginSession(
            User user,
            HttpServletResponse response) {

        refreshTokenRepository.revokeAllByUser(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        CookieUtils.createCookie(response, "accessToken", accessToken, 900);
        CookieUtils.createCookie(response, "refreshToken", refreshTokenValue, 604800);

        return LoginResponse.builder()
                .expiresIn(900)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}