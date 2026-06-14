package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request must not be null");
        }

        String name = request.getName() == null ? null : request.getName().trim();
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        String password = request.getPassword();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (userRepository.existsByPhone(phone)) {
            throw new ResourceConflictException("Phone number already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email already registered");
        }

        String hash = passwordEncoder.encode(password);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(hash);
        user.setRole(User.Role.USER);
        user.setUserStatus(User.UserStatus.ACTIVE);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setWalletStatus(Wallet.WalletStatus.ACTIVE);

        user.setWallet(wallet);

        userRepository.save(user);
    }
}