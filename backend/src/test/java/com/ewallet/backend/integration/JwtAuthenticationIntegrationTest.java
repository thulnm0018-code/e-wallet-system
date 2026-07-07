package com.ewallet.backend.integration;

import com.ewallet.backend.BackendApplication;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User();
        user.setName("JWT User");
        user.setEmail("jwt@example.com");
        user.setPhone("0987654325");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(User.Role.USER);
        user.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        user = userRepository.save(user);
        token = jwtTokenProvider.generateAccessToken(user);
    }

    @Test
    void protectedEndpoint_shouldBeAccessibleWithJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email")
                .value("jwt@example.com"));
    }

    @Test
    void protectedEndpoint_shouldFailWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldFailWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
void currentUser_shouldReturnAuthenticatedUser()
        throws Exception {

    mockMvc.perform(get("/api/v1/auth/me")
            .header(
                "Authorization",
                "Bearer " + token
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                    .value("Fetch user session successful"))
            .andExpect(jsonPath("$.data.email")
                    .value("jwt@example.com"));
}

    @Test
void logout_shouldReturnSuccess()
        throws Exception {

    mockMvc.perform(post("/api/v1/auth/logout")
            .header(
                "Authorization",
                "Bearer " + token
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message")
                    .value("Logged out successfully"));
}
}
