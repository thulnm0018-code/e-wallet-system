package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.ForgotPasswordRequest;
import com.ewallet.backend.dto.request.ResetPasswordRequest;
import com.ewallet.backend.dto.request.UpdateProfileRequest;
import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminTransactionResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.ReceiverLookupResponse;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.UserService;
import com.ewallet.backend.util.OtpUtils;
import com.ewallet.backend.util.PhoneUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpRepository otpRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           OtpRepository otpRepository,
                           WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpRepository = otpRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        String name = normalizeRequiredText(request.getName(), "Name");
        String email = normalizeEmail(request.getEmail());
        String phone = PhoneUtils.normalize(request.getPhone());

        if (phone == null) {
            throw new IllegalArgumentException("Invalid phone number format or unsupported country code");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new ResourceConflictException("Phone number already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email already registered");
        }

        String password = normalizeRequiredText(request.getPassword(), "Password");
        String hash = passwordEncoder.encode(password);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(hash);
        user.setRole(User.Role.USER);
        user.setUserStatus(UserStatus.PENDING_VERIFICATION);

        User savedUser = userRepository.save(user);

        String otpCode = OtpUtils.generateOtp();

        Otp otp = Otp.builder()
                .user(savedUser)
                .otpCode(otpCode)
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(Objects.requireNonNull(otp));

        log.info("==========================================");
        log.info("MA OTP KiCH HOAT TAI KHOAN CHO [{}]: {}", phone, otpCode);
        log.info("==========================================");

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    public ReceiverLookupResponse getUserByPhone(String phone) {
        String cleanPhone = PhoneUtils.normalize(phone);
        if (cleanPhone == null) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        User user = userRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new NotFoundException("User not found with phone: " + phone));

        Wallet wallet = walletRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Wallet not found for user: " + user.getId()));

        return ReceiverLookupResponse.builder()
                .name(user.getName())
                .walletId(wallet.getId())
                .walletStatus(wallet.getWalletStatus())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        String identifier = normalizeIdentifier(request.getIdentifier());

        User user = resolveUserByIdentifier(identifier);
        String otpCode = OtpUtils.generateOtp();

        Otp otp = Otp.builder()
                .user(user)
                .otpCode(otpCode)
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(Objects.requireNonNull(otp));
        log.info("Password reset OTP for {}: {}", identifier, otpCode);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        String identifier = normalizeIdentifier(request.getIdentifier());
        String newPassword = normalizeRequiredText(request.getNewPassword(), "New password");

        User user = resolveUserByIdentifier(identifier);
        Otp otp = otpRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new NotFoundException("No password reset OTP found"));

        if (otp.isVerified()) {
            throw new BadRequestException("OTP already used");
        }
        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        otp.setVerified(true);
        otpRepository.save(otp);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {


Long userId = Objects.requireNonNull(
        currentUserService.getCurrentUserId(),
        "Current user id is null"
);

User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
        String normalizedCurrentPassword = currentPassword == null ? "" : currentPassword;
        String normalizedNewPassword = newPassword == null ? "" : newPassword;

        if (!StringUtils.hasText(normalizedCurrentPassword) || !StringUtils.hasText(normalizedNewPassword)) {
            throw new BadRequestException("Password fields are required");
        }

        if (!passwordEncoder.matches(normalizedCurrentPassword, user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(normalizedNewPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        Long userId = Objects.requireNonNull(
        currentUserService.getCurrentUserId(),"Current user id is null");

        User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));


        String name = normalizeRequiredText(request.getName(), "Name");
        String email = normalizeEmail(request.getEmail());
        String phone = PhoneUtils.normalize(request.getPhone());

        if (phone == null) {
            throw new BadRequestException("Invalid phone number format");
        }

        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email already registered");
        }
        if (!phone.equals(user.getPhone()) && userRepository.existsByPhone(phone)) {
            throw new ResourceConflictException("Phone already registered");
        }

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setUpdatedAt(LocalDateTime.now());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    public List<AdminUserResponse> getAdminUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Wallet wallet = walletRepository.findByUser_Id(user.getId()).orElse(null);
                    return AdminUserResponse.fromEntity(user, wallet != null ? wallet.getBalance() : BigDecimal.ZERO);
                })
                .toList();
    }

    @Override
    public List<AdminTransactionResponse> getAdminTransactions() {
        return transactionRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(AdminTransactionResponse::fromEntity)
                .toList();
    }

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        List<Transaction> transactions = transactionRepository.findAll();
        long pendingReviews = transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.PENDING)
                .count();

        BigDecimal totalVolume = transactions.stream()
        .filter(t -> t != null)
        .map(t -> t.getAmount())
        .filter(amount -> amount != null)
        .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));


        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeWallets(walletRepository.count())
                .totalVolume(totalVolume)
                .pendingReviews(pendingReviews)
                .build();
    }

    private User resolveUserByIdentifier(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);

        if (normalizedIdentifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return userRepository.findByEmail(normalizedIdentifier.toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }

        String normalizedPhone = PhoneUtils.normalize(normalizedIdentifier);
        if (normalizedPhone == null) {
            throw new BadRequestException("Invalid identifier");
        }

        return userRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeIdentifier(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException("Identifier is required");
        }
        return value.trim();
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        return normalizeRequiredText(value, "Email").toLowerCase(Locale.ROOT);
    }
}