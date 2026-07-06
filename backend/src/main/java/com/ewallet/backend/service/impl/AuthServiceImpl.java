package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.RefreshToken;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.exception.UnauthorizedException;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.RefreshTokenRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.service.AuthService;
import com.ewallet.backend.security.jwt.JwtTokenProvider;
import com.ewallet.backend.util.CookieUtils;
import com.ewallet.backend.util.PhoneUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final WalletRepository walletRepository;
    private final OtpRepository otpRepository;

    public AuthServiceImpl(UserRepository userRepository, 
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder, 
                           JwtTokenProvider jwtTokenProvider,
                           WalletRepository walletRepository,
                           OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.walletRepository = walletRepository;
        this.otpRepository = otpRepository;
    }

    @Override
    @Transactional
    public LoginResponse login(UserLoginRequest request, HttpServletResponse response) {
        if (request == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String input = request.getIdentifier() == null ? "" : request.getIdentifier().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (!StringUtils.hasText(input) || !StringUtils.hasText(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }

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
        // Check if user is pending verification
        if (user.getUserStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new UnauthorizedException("Account is not activated. Please verify your OTP.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
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

            if (newRefreshToken == null) {
        throw new IllegalArgumentException("Refresh token cannot be null");
            }

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

        refreshTokenRepository.save(Objects.requireNonNull(refreshToken));

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

@Override
@Transactional
public void verifyOtp(String phoneOrEmail, String otpCode) {

    String input = phoneOrEmail == null ? "" : phoneOrEmail.trim();
    if (!StringUtils.hasText(input) || !StringUtils.hasText(otpCode)) {
        throw new RuntimeException("Invalid OTP");
    }

    boolean isEmail = input.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    String normalizedIdentifier = input;
    if (isEmail) {
        normalizedIdentifier = input.toLowerCase();
    } else {
        String cleanPhone = PhoneUtils.normalize(input);
        if (cleanPhone != null) {
            normalizedIdentifier = cleanPhone;
        }
    }

    Otp otp = otpRepository
            .findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc(
                    normalizedIdentifier,
                    normalizedIdentifier)
            .orElseThrow(
                    () -> new RuntimeException(
                            "OTP does not exist"));

    if (otp.isVerified()) {
        throw new RuntimeException(
                "OTP already used");
    }

    if (!otp.getOtpCode().equals(otpCode)) {
        throw new RuntimeException(
                "Invalid OTP");
    }

    if (otp.getExpiredAt()
            .isBefore(LocalDateTime.now())) {
        throw new RuntimeException(
                "OTP expired");
    }

    User user = otp.getUser();

    user.setUserStatus(UserStatus.ACTIVE);

    userRepository.save(user);

    if (user.getWallet() == null) {

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .walletStatus(WalletStatus.ACTIVE)
                .build();

     walletRepository.save(Objects.requireNonNull(wallet));

        user.setWallet(wallet);
    }

    otp.setVerified(true);

    otpRepository.save(otp);
}
}