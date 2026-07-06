package com.ewallet.backend.service;

import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.CurrentUserService;
import com.ewallet.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void changePassword_shouldUpdateHashWhenCurrentPasswordMatches() {
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("old-hash");

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");

        userService.changePassword("current123", "newPassword123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        verify(userRepository).save(user);
    }
}

