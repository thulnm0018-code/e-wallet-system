package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.dto.response.LoginResponse;
import com.ewallet.backend.entity.RefreshToken;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.UserStatus;
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

import java.time.LocalDateTime;
import java.util.Optional;

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

		when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
		when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access-token");
		when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
		doNothing().when(refreshTokenRepository).revokeAllByUser(user);
		when(refreshTokenRepository.save(any(RefreshToken.class)))
				.thenAnswer(inv -> {
					RefreshToken t = inv.getArgument(0);
					t.setId(1L);
					t.setCreatedAt(LocalDateTime.now());
					return t;
				});

		LoginResponse resp = authService.login(req, response);

        verify(userRepository).findByEmail("alice@example.com");

        verify(passwordEncoder).matches("secret", "hashed");

        verify(jwtTokenProvider).generateAccessToken(user);

        verify(jwtTokenProvider).generateRefreshToken(user);

		Assertions.assertNotNull(resp);
		Assertions.assertEquals(900, resp.getExpiresIn());
		Assertions.assertNotNull(resp.getUser());

		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	void login_withInvalidCredentials_throws() {
		UserLoginRequest req = new UserLoginRequest();
		req.setIdentifier("bob@example.com");
		req.setPassword("nope");

		when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.empty());

		Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(req, response));
	}

	@Test
	void login_withWrongPassword_throws() {
		User user = new User();
		user.setId(2L);
		user.setEmail("charlie@example.com");
		user.setPasswordHash("hashed");
		user.setUserStatus(UserStatus.ACTIVE);

		UserLoginRequest req = new UserLoginRequest();
		req.setIdentifier("charlie@example.com");
		req.setPassword("wrong-pass");

		when(userRepository.findByEmail("charlie@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-pass", "hashed")).thenReturn(false);

		Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(req, response));

		verify(userRepository).findByEmail("charlie@example.com");
		verify(passwordEncoder).matches("wrong-pass", "hashed");
	}

	@Test
void login_withPendingVerification_throws() {

    User user = new User();
    user.setId(3L);
    user.setEmail("dave@example.com");
    user.setPasswordHash("hashed");
    user.setUserStatus(UserStatus.PENDING_VERIFICATION);

    UserLoginRequest req = new UserLoginRequest();
    req.setIdentifier("dave@example.com");
    req.setPassword("secret");

    when(userRepository.findByEmail("dave@example.com")).thenReturn(Optional.of(user));

    Assertions.assertThrows(UnauthorizedException.class,() -> authService.login(req, response));

    verify(userRepository).findByEmail("dave@example.com");

	verify(passwordEncoder, never()).matches(any(), any());
}

	@Test
	void login_withNullRequest_throws() {
		Assertions.assertThrows(UnauthorizedException.class, () -> authService.login(null, response));
	}
}
