package com.ewallet.backend.integration;

import com.ewallet.backend.BackendApplication;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String token;
    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        walletRepository.deleteAll();
        otpRepository.deleteAll();

        String senderPhone = PhoneUtils.normalize("0987654323");
        String receiverPhone = PhoneUtils.normalize("0987654324");

        sender = new User();
        sender.setName("Sender");
        sender.setEmail("sender@example.com");
        sender.setPhone(senderPhone);
        sender.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        sender.setRole(User.Role.USER);
        sender.setUserStatus(UserStatus.ACTIVE);
        sender = userRepository.save(sender);

        senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));
        senderWallet.setWalletStatus(WalletStatus.ACTIVE);
        walletRepository.save(senderWallet);

        receiver = new User();
        receiver.setName("Receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPhone(receiverPhone);
        receiver.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        receiver.setRole(User.Role.USER);
        receiver.setUserStatus(UserStatus.ACTIVE);
        receiver = userRepository.save(receiver);

        receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("50.00"));
        receiverWallet.setWalletStatus(WalletStatus.ACTIVE);
        walletRepository.save(receiverWallet);

        token = jwtTokenProvider.generateAccessToken(sender);
    }

    @Test
    void getMyWallet_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyWallet_shouldReturnWalletForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @SuppressWarnings("null")
    @Test
    void transfer_shouldReturnSuccessForValidOtp() throws Exception {
        User sender = userRepository.findByEmail("sender@example.com").orElseThrow();
        Otp otp = Otp.builder()
                .user(sender)
                .otpCode("123456")
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .amount(new BigDecimal("10.00"))
                .receiverPhone(PhoneUtils.normalize("0987654324"))
                .build();
        otpRepository.save(otp);

        TransferRequest request = new TransferRequest();
        request.setReceiverPhone("0987654324");
        request.setAmount(new BigDecimal("10.00"));
        request.setOtpCode("123456");
        request.setMessage("Test transfer");

        mockMvc.perform(post("/api/v1/wallets/transfer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transfer successful"));

        Wallet updatedSender = walletRepository.findByUser_Id(sender.getId()).orElseThrow();
        Wallet updatedReceiver = walletRepository.findByUser_Id(receiver.getId()).orElseThrow();

        org.assertj.core.api.Assertions.assertThat(updatedSender.getBalance()).isEqualByComparingTo(new BigDecimal("90.00"));
        org.assertj.core.api.Assertions.assertThat(updatedReceiver.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void history_shouldReturnPaginatedTransactionsForAuthenticatedUser() throws Exception {
        Transaction tx = Transaction.builder()
                .transactionCode("TX-001")
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(new BigDecimal("15.00"))
                .message("History sample")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build();
        transactionRepository.save(tx);

        mockMvc.perform(get("/api/v1/wallets/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
