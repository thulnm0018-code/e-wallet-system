package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.response.UserResponse;
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
    public UserResponse registerUser(UserCreateRequest request) {

      String email = request.getEmail().trim().toLowerCase();
      String phone = request.getPhone().trim();

        if (userRepository.existsByPhone(phone)) {
            throw new ResourceConflictException("Phone number already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Email already registered");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName().trim());
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

        User savedUser = userRepository.save(user);
        
       return UserResponse.fromEntity(savedUser);
    }
}