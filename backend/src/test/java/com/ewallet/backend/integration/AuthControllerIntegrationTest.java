package com.ewallet.backend.integration;

import com.ewallet.backend.BackendApplication;
import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.repository.UserRepository;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @SuppressWarnings("null")
    @Test
    void register_shouldCreateUserAndReturnCreated() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPhone("0987654321");
        request.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Please verify OTP."))
                .andExpect(jsonPath("$.data.id").value(notNullValue()));

        User savedUser = userRepository.findByEmail("alice@example.com").orElseThrow();
        assert savedUser.getPasswordHash() != null;
    }

    @SuppressWarnings("null")
    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() throws Exception {
        User user = new User();
        user.setName("Bob");
        user.setEmail("bob@example.com");
        user.setPhone("0987654322");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(User.Role.USER);
        user.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        userRepository.save(user);

        UserLoginRequest request = new UserLoginRequest();
        request.setIdentifier("bob@example.com");
        request.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andExpect(jsonPath("$.data.user").exists())
                .andExpect(jsonPath("$.data.user.email").value("bob@example.com"));
    }

    @SuppressWarnings("null")
    @Test
    void register_shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        User existing = new User();
        existing.setName("Existing");
        existing.setEmail("duplicate@example.com");
        existing.setPhone("0987654399");
        existing.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        existing.setRole(User.Role.USER);
        existing.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        userRepository.save(existing);

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Dup");
        request.setEmail("duplicate@example.com");
        request.setPhone("0987654400");
        request.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @SuppressWarnings("null")
    @Test
    void login_shouldReturnUnauthorizedWhenPasswordIsWrong() throws Exception {
        User user = new User();
        user.setName("Carol");
        user.setEmail("carol@example.com");
        user.setPhone("0987654401");
        user.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        user.setRole(User.Role.USER);
        user.setUserStatus(com.ewallet.backend.enums.UserStatus.ACTIVE);
        userRepository.save(user);

        UserLoginRequest request = new UserLoginRequest();
        request.setIdentifier("carol@example.com");
        request.setPassword("WrongPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
