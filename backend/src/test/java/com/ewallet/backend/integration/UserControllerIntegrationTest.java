package com.ewallet.backend.integration;

import com.ewallet.backend.BackendApplication;
import com.ewallet.backend.dto.request.ChangePasswordRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.jwt.JwtTokenProvider;
import com.ewallet.backend.util.PhoneUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User();
        user.setName("User One");
        user.setEmail("userone@example.com");
        user.setPhone("0987654326");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(User.Role.USER);
        user.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        user = userRepository.save(user);
        token = jwtTokenProvider.generateAccessToken(user);
    }

    @SuppressWarnings("null")
    @Test
    void changePassword_shouldSucceedForAuthenticatedUser() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("StrongPass123");
        request.setNewPassword("NewStrongPass123");

        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByPhone_shouldReturnUserDataForAuthenticatedUser() throws Exception {
        User target = new User();
        target.setName("Target User");
        target.setEmail("target@example.com");
        target.setPhone(PhoneUtils.normalize("0987654444"));
        target.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        target.setRole(User.Role.USER);
        target.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        target = userRepository.save(target);

        Wallet wallet = new Wallet();
        wallet.setUser(target);
        wallet.setBalance(java.math.BigDecimal.ZERO);
        wallet.setWalletStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);

        mockMvc.perform(get("/api/v1/users/phone/{phone}", "0987654444")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Target User"));
    }
}
