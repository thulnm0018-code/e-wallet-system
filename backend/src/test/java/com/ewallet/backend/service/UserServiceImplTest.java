package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    private TransactionRepository transactionRepository;

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

    @SuppressWarnings("null")
    @Test
    void changePassword_shouldRejectIncorrectCurrentPassword() {
        User user = new User();
        user.setId(2L);
        user.setPasswordHash("old-hash");

        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", "old-hash")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.changePassword("wrong-current", "newPassword123"));

        assertThat(exception.getMessage()).isEqualTo("Current password is incorrect");
        verify(userRepository, never()).save(any(User.class));
    }

    @SuppressWarnings("null")
    @Test
    void registerUser_shouldThrowConflictWhenEmailAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPhone("0987654321");
        request.setPassword("StrongPass123");

        when(userRepository.existsByPhone("+84987654321")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> userService.registerUser(request));

        assertThat(exception.getMessage()).isEqualTo("Email already registered");
        verify(otpRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldThrowConflictWhenPhoneExists() {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPhone("0987654321");
        request.setPassword("StrongPass123");

        when(userRepository.existsByPhone("+84987654321")).thenReturn(true);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class,
                () -> userService.registerUser(request));

        assertThat(exception.getMessage()).isEqualTo("Phone number already registered");
        verify(otpRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowWhenUserNotFound() {
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.changePassword("current123", "newPassword123"));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        verify(userRepository, never()).save(any(User.class));
    }
}

