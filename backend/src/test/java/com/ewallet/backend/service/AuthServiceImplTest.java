package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.RefreshToken;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.exception.UnauthorizedException;
import com.ewallet.backend.repository.RefreshTokenRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private com.ewallet.backend.repository.WalletRepository walletRepository;

    @Mock
    private com.ewallet.backend.repository.OtpRepository otpRepository;

    @InjectMocks
    private com.ewallet.backend.service.impl.AuthServiceImpl authService;

    @Mock
    private HttpServletResponse response;

    @SuppressWarnings("null")
	@Test
    void login_withEmail_success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@example.com");
        user.setPhone("+84123456789");
        user.setPasswordHash("hashed");
        user.setUserStatus(UserStatus.ACTIVE);

        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("alice@example.com");
        req.setPassword("secret");

        when(userRepository.findByEmailAndDeletedFalse("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        doNothing().when(refreshTokenRepository).revokeAllByUser(user);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> {
                    RefreshToken token = inv.getArgument(0);
                    token.setId(1L);
                    token.setCreatedAt(LocalDateTime.now());
                    return token;
                });

        LoginResponse resp = authService.login(req, response);

        verify(userRepository).findByEmailAndDeletedFalse("alice@example.com");
        verify(passwordEncoder).matches("secret", "hashed");
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(jwtTokenProvider).generateRefreshToken(user);

        Assertions.assertNotNull(resp);
        Assertions.assertEquals(900, resp.getExpiresIn());
        Assertions.assertNotNull(resp.getUser());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_withPhoneIdentifierAndValidCredentials_success() {
        User user = new User();
        user.setId(2L);
        user.setEmail("bob@example.com");
        user.setPhone("+84987654321");
        user.setPasswordHash("hashed");
        user.setUserStatus(UserStatus.ACTIVE);

        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("0987654321");
        req.setPassword("secret");

        when(userRepository.findByPhoneAndDeletedFalse("+84987654321")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginResponse resp = authService.login(req, response);

        assertThat(resp).isNotNull();
        assertThat(resp.getUser()).isNotNull();
        verify(userRepository).findByPhoneAndDeletedFalse("+84987654321");
    }

    @Test
    void login_withInvalidCredentials_throws() {
        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("bob@example.com");
        req.setPassword("nope");

        when(userRepository.findByEmailAndDeletedFalse("bob@example.com")).thenReturn(Optional.empty());

        Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(req, response));
    }

    @Test
    void login_withWrongPassword_throws() {
        User user = new User();
        user.setId(3L);
        user.setEmail("charlie@example.com");
        user.setPasswordHash("hashed");
        user.setUserStatus(UserStatus.ACTIVE);

        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("charlie@example.com");
        req.setPassword("wrong-pass");

        when(userRepository.findByEmailAndDeletedFalse("charlie@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "hashed")).thenReturn(false);

        Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(req, response));

        verify(userRepository).findByEmailAndDeletedFalse("charlie@example.com");
        verify(passwordEncoder).matches("wrong-pass", "hashed");
    }

    @Test
    void login_withPendingVerification_throws() {
        User user = new User();
        user.setId(4L);
        user.setEmail("dave@example.com");
        user.setPasswordHash("hashed");
        user.setUserStatus(UserStatus.PENDING_VERIFICATION);

        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("dave@example.com");
        req.setPassword("secret");

        when(userRepository.findByEmailAndDeletedFalse("dave@example.com")).thenReturn(Optional.of(user));

        Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(req, response));

        verify(userRepository).findByEmailAndDeletedFalse("dave@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_withEmptyPassword_throwsUnauthorized() {
        UserLoginRequest req = new UserLoginRequest();
        req.setIdentifier("eve@example.com");
        req.setPassword("");

        UnauthorizedException exception = Assertions.assertThrows(UnauthorizedException.class,
                () -> authService.login(req, response));

        assertThat(exception.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void login_withNullRequest_throws() {
        Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(null, response));
    }

    @SuppressWarnings("null")
	@Test
    void verifyOtp_withValidOtpActivatesUserAndCreatesWallet() {
        User user = new User();
        user.setId(5L);
        user.setEmail("frank@example.com");
        user.setPhone("+84987654322");
        user.setUserStatus(UserStatus.PENDING_VERIFICATION);

        Otp otp = Otp.builder()
                .id(10L)
                .user(user)
                .otpCode("123456")
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc("frank@example.com", "frank@example.com"))
                .thenReturn(Optional.of(otp));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(otpRepository.save(any(Otp.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.verifyOtp("frank@example.com", "123456");

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getWallet()).isNotNull();
        assertThat(user.getWallet().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(user.getWallet().getWalletStatus()).isEqualTo(WalletStatus.ACTIVE);
        verify(userRepository).save(user);
        verify(walletRepository).save(any(Wallet.class));
        verify(otpRepository).save(otp);
    }

    @Test
    void verifyOtp_whenNotFound_throws() {
        when(otpRepository.findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc("missing@example.com", "missing@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> authService.verifyOtp("missing@example.com", "123456"));

        assertThat(exception.getMessage()).isEqualTo("OTP does not exist");
    }

    @Test
    void verifyOtp_whenInvalid_throws() {
        User user = new User();
        user.setId(6L);
        user.setEmail("grace@example.com");
        user.setPhone("+84987654323");

        Otp otp = Otp.builder()
                .id(11L)
                .user(user)
                .otpCode("654321")
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc("grace@example.com", "grace@example.com"))
                .thenReturn(Optional.of(otp));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> authService.verifyOtp("grace@example.com", "123456"));

        assertThat(exception.getMessage()).isEqualTo("Invalid OTP");
    }

    @Test
    void verifyOtp_whenExpired_throws() {
        User user = new User();
        user.setId(7L);
        user.setEmail("henry@example.com");
        user.setPhone("+84987654324");

        Otp otp = Otp.builder()
                .id(12L)
                .user(user)
                .otpCode("123456")
                .verified(false)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(otpRepository.findTopByUser_EmailOrUser_PhoneOrderByCreatedAtDesc("henry@example.com", "henry@example.com"))
                .thenReturn(Optional.of(otp));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> authService.verifyOtp("henry@example.com", "123456"));

        assertThat(exception.getMessage()).isEqualTo("OTP expired");
    }
}
