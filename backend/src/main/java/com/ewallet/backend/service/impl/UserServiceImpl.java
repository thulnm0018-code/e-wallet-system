package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.service.UserService;
import com.ewallet.backend.util.PhoneUtils;
import com.ewallet.backend.util.OtpUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpRepository otpRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpRepository = otpRepository;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {

      String email = request.getEmail().trim().toLowerCase();
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

        String hash = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName().trim());
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

        otpRepository.save(otp);

       System.out.println("==========================================");
        System.out.println("MA OTP KiCH HOAT TAI KHOAN CHO [" + phone + "]: " + otpCode);
        System.out.println("==========================================");
        
        return UserResponse.fromEntity(savedUser);
    }
}