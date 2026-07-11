package com.ewallet.backend.integration;

import com.ewallet.backend.BackendApplication;
import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.request.UserLoginRequest;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class MysqlContainerSmokeTest {

    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ewallet_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @SuppressWarnings("null")
    @Test
    void registerAndLoginWorkWithMysqlContainer() throws Exception {
        UserCreateRequest registerRequest = new UserCreateRequest();
        registerRequest.setName("MySQL User");
        registerRequest.setEmail("mysql-user@example.com");
        registerRequest.setPhone("0987654445");
        registerRequest.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        User savedUser = userRepository.findByEmailAndDeletedFalse("mysql-user@example.com").orElseThrow();
        savedUser.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(savedUser);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setIdentifier("mysql-user@example.com");
        loginRequest.setPassword("StrongPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
