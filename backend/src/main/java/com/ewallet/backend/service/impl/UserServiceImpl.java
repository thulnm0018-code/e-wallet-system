package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;  // dung de tuong tac voi database, luu tru user va truy van user
    private final PasswordEncoder passwordEncoder; // dependency injection, de ma hoa mat khau truoc khi luu vao database

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(UserCreateRequest request) {

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        String hash = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName());  // lay (get) tu request, sau do gan (set) vao user entity
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(hash);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        user.setWallet(wallet);

        userRepository.save(user);
    }
}