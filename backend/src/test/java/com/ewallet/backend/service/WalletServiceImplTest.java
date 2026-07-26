package com.ewallet.backend.service;

import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.entity.WithdrawalRequest;
import com.ewallet.backend.enums.AuditAction;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.enums.WithdrawalStatus;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.mapper.TransactionMapper;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.repository.WithdrawalRequestRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.impl.WalletServiceImpl;
import com.ewallet.backend.util.TransactionCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCodeGenerator codeGenerator;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @Mock
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RabbitProducerService rabbitProducerService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("0987654321");
        testUser.setName("Test User");
        testUser.setUserStatus(UserStatus.ACTIVE);
        testUser.setAddress("123 Test St");
        testUser.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));

        testWallet = Wallet.builder()
                .id(10L)
                .user(testUser)
                .balance(new BigDecimal("125.50"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();
    }

    @Test
    void getMyWalletShouldReturnCurrentUserWalletInfo() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));

        var response = walletService.getMyWallet();

        verify(currentUserService).getCurrentUserId();
        verify(walletRepository).findByUser_Id(1L);

        assertEquals(10L, response.getId());
        assertEquals("125.50", response.getBalance().toPlainString());
        assertEquals("ACTIVE", response.getWalletStatus());
        assertEquals("Test User", response.getName());
        assertEquals("0987654321", response.getPhone());
    }

    @Test
    void getMyWalletShouldThrowWhenWalletNotFound() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> walletService.getMyWallet());

        assertEquals("Wallet not found", exception.getMessage());
        verify(currentUserService).getCurrentUserId();
        verify(walletRepository).findByUser_Id(1L);
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldSucceed() {
        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setPhone("0987654322");
        receiverUser.setName("Receiver User");
        receiverUser.setUserStatus(UserStatus.ACTIVE);

        Wallet receiverWallet = Wallet.builder()
                .id(20L)
                .user(receiverUser)
                .balance(new BigDecimal("50.00"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        Otp otp = Otp.builder()
                .id(1L)
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654322")).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(receiverWallet));
        when(codeGenerator.generate()).thenReturn("TXN-123");

        Transaction savedTransaction = Transaction.builder()
                .id(100L)
                .transactionCode("TXN-123")
                .senderWallet(testWallet)
                .receiverWallet(receiverWallet)
                .amount(new BigDecimal("25.50"))
                .message("Payment")
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponse.builder()
                .id(100L)
                .transactionCode("TXN-123")
                .senderPhone("0987654321")
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .message("Payment")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build());

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        TransactionResponse response = walletService.transferMoney(request);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionCode()).isEqualTo("TXN-123");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.getReceiverPhone()).isEqualTo("0987654322");
        assertThat(testWallet.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("75.50"));
        verify(walletRepository).saveAll(anyList());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @SuppressWarnings("null")
    @Test
    void shouldRejectDuplicateTransferRequest() throws Exception {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        Field field = WalletServiceImpl.class.getDeclaredField("processingTransfers");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Boolean> processingTransfers = (ConcurrentHashMap<String, Boolean>) field.get(walletService);
        processingTransfers.put("1-25.50-0987654322", Boolean.TRUE);

        ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> walletService.transferMoney(request));

        assertEquals("Transfer already being processed", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenReceiverNotFound() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654322")).thenReturn(Optional.empty());

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> walletService.transferMoney(request));

        assertEquals("Receiver wallet not found", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenTransferToSelf() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654321")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654321")).thenReturn(Optional.of(testWallet));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654321")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("Cannot transfer money to yourself", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenOtpIsExpired() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("OTP expired", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenAmountLessThanOrEqualToZero() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(BigDecimal.ZERO)
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenOtpInvalid() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("654321")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("Invalid OTP", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenInsufficientBalance() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("200.00"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setPhone("0987654322");
        receiverUser.setUserStatus(UserStatus.ACTIVE);

        Wallet receiverWallet = Wallet.builder()
                .id(20L)
                .user(receiverUser)
                .balance(new BigDecimal("50.00"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654322")).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(receiverWallet));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("200.00"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("Insufficient balance", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
@Test
    void transferMoneyShouldThrowWhenOtpAlreadyUsed() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(true)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("OTP already used", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void shouldRejectExpiredOtp() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("OTP expired", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldLockOtpAfterFiveFailures() {
        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .failedAttempts(4)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("654321")
                .message("Payment")
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> walletService.transferMoney(request));

        assertEquals("OTP locked. Please request a new OTP.", exception.getMessage());
        assertThat(otp.isVerified()).isTrue();
        assertThat(otp.getFailedAttempts()).isEqualTo(5);
        verify(otpRepository).save(otp);
    }

    @SuppressWarnings("null")
    @Test
    void shouldCreatePendingWithdrawal() {
        testWallet.setBalance(new BigDecimal("5000.00"));

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("3500.00"));
        request.setMessage("Pending withdrawal");

        TransactionResponse response = walletService.withdrawMoney(request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("3500.00"));
        verify(withdrawalRequestRepository).save(any(WithdrawalRequest.class));
    }

    @SuppressWarnings("null")
    @Test
    void shouldCreateAuditLogAfterTransfer() {
        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setPhone("0987654322");
        receiverUser.setName("Receiver User");
        receiverUser.setUserStatus(UserStatus.ACTIVE);

        Wallet receiverWallet = Wallet.builder()
                .id(20L)
                .user(receiverUser)
                .balance(new BigDecimal("50.00"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654322")).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(receiverWallet));
        when(codeGenerator.generate()).thenReturn("TXN-999");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(TransactionResponse.builder()
                .transactionCode("TXN-999")
                .amount(new BigDecimal("25.50"))
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build());

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        walletService.transferMoney(request);

        verify(auditLogService).log(eq(testUser), eq(AuditAction.TRANSFER), anyString());
    }

    @SuppressWarnings("null")
    @Test
    void shouldSendRabbitNotificationAfterTransfer() {
        User receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setPhone("0987654322");
        receiverUser.setName("Receiver User");
        receiverUser.setUserStatus(UserStatus.ACTIVE);

        Wallet receiverWallet = Wallet.builder()
                .id(20L)
                .user(receiverUser)
                .balance(new BigDecimal("50.00"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        Otp otp = Otp.builder()
                .user(testUser)
                .otpCode("123456")
                .receiverPhone("+84987654322")
                .amount(new BigDecimal("25.50"))
                .verified(false)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(otpRepository.findTopByUserOrderByCreatedAtDesc(testUser)).thenReturn(Optional.of(otp));
        when(walletRepository.findByUser_Phone("+84987654322")).thenReturn(Optional.of(receiverWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(receiverWallet));
        when(codeGenerator.generate()).thenReturn("TXN-998");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(TransactionResponse.builder()
                .transactionCode("TXN-998")
                .amount(new BigDecimal("25.50"))
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build());

        TransferRequest request = TransferRequest.builder()
                .receiverPhone("0987654322")
                .amount(new BigDecimal("25.50"))
                .otpCode("123456")
                .message("Payment")
                .build();

        walletService.transferMoney(request);

        verify(rabbitProducerService, times(2)).sendNotification(any(NotificationMessage.class));
    }

    @SuppressWarnings("null")
@Test
    void depositMoneyShouldIncreaseBalanceAndCreateTransaction() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(testWallet));
        when(codeGenerator.generate()).thenReturn("DEP-001");

        Transaction savedTransaction = Transaction.builder()
                .id(200L)
                .transactionCode("DEP-001")
                .receiverWallet(testWallet)
                .amount(new BigDecimal("50.00"))
                .message("Deposit")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(transactionMapper.toResponse(savedTransaction)).thenReturn(TransactionResponse.builder()
                .id(200L)
                .transactionCode("DEP-001")
                .amount(new BigDecimal("50.00"))
                .message("Deposit")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.DEPOSIT)
                .build());

        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setMessage("Deposit");

        TransactionResponse response = walletService.depositMoney(request);

        assertThat(response.getTransactionCode()).isEqualTo("DEP-001");
        assertThat(testWallet.getBalance()).isEqualByComparingTo(new BigDecimal("175.50"));
        verify(walletRepository).save(testWallet);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @SuppressWarnings("null")
@Test
    void getMyHistoryShouldReturnTransactionList() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));

        Transaction transaction = Transaction.builder()
                .id(42L)
                .transactionCode("TXN-42")
                .senderWallet(testWallet)
                .amount(new BigDecimal("15.00"))
                .message("History test")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(42L)
                .transactionCode("TXN-42")
                .senderPhone("0987654321")
                .receiverPhone("0987654322")
                .amount(new BigDecimal("15.00"))
                .message("History test")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build();

        when(transactionRepository.findWalletTransactions(eq(10L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(transaction)));
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        var history = walletService.getMyHistory(null, null, null, 0, 20);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getTransactionCode()).isEqualTo("TXN-42");
    }

    @SuppressWarnings("null")
@Test
    void getMyHistoryShouldReturnEmptyList() {
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findWalletTransactions(eq(10L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var history = walletService.getMyHistory(null, null, null, 0, 20);

        assertThat(history).isEmpty();
    }

    @SuppressWarnings("null")
    @Test
void shouldReturnExistingTransactionWhenIdempotencyKeyExists() {

    Transaction existingTransaction =
            Transaction.builder()
                    .id(1L)
                    .transactionCode("TXN-EXISTING")
                    .idempotencyKey("abc-123")
                    .build();

    TransactionResponse existingResponse =
            TransactionResponse.builder()
                    .transactionCode("TXN-EXISTING")
                    .build();

    when(
            transactionRepository.findByIdempotencyKey(
                    "abc-123"
            )
    ).thenReturn(
            Optional.of(existingTransaction)
    );

    when(
            transactionMapper.toResponse(
                    existingTransaction
            )
    ).thenReturn(existingResponse);

    TransferRequest request =
            TransferRequest.builder()
                    .receiverPhone("0987654322")
                    .amount(new BigDecimal("25.50"))
                    .otpCode("123456")
                    .idempotencyKey("abc-123")
                    .build();

    TransactionResponse response =
            walletService.transferMoney(request);

    assertEquals(
            "TXN-EXISTING",
            response.getTransactionCode()
    );

    verify(otpRepository, never())
        .findTopByUserOrderByCreatedAtDesc(any());
        
    verify(
            transactionRepository,
            never()
    ).save(any(Transaction.class));

    verify(
            walletRepository,
            never()
    ).saveAll(anyList());
}

@SuppressWarnings("null")
@Test
void shouldReturnExistingWithdrawTransactionWhenIdempotencyKeyExists() {

    Transaction existingTransaction =
            Transaction.builder()
                    .id(1L)
                    .transactionCode("WD-001")
                    .idempotencyKey("withdraw-success")
                    .type(TransactionType.WITHDRAW)
                    .status(TransactionStatus.SUCCESS)
                    .build();

    TransactionResponse existingResponse =
            TransactionResponse.builder()
                    .transactionCode("WD-001")
                    .status(TransactionStatus.SUCCESS)
                    .type(TransactionType.WITHDRAW)
                    .build();

    when(
            transactionRepository.findByIdempotencyKey(
                    "withdraw-success"
            )
    ).thenReturn(Optional.of(existingTransaction));

    when(
            transactionMapper.toResponse(
                    existingTransaction
            )
    ).thenReturn(existingResponse);

    WithdrawRequest request =
            new WithdrawRequest();

    request.setAmount(
            new BigDecimal("100")
    );

    request.setIdempotencyKey(
            "withdraw-success"
    );

    TransactionResponse response =
            walletService.withdrawMoney(
                    request
            );

    assertEquals(
            "WD-001",
            response.getTransactionCode()
    );

    verify(
            transactionRepository,
            never()
    ).save(any(Transaction.class));
} 

@SuppressWarnings("null")
@Test
void shouldReturnExistingPendingWithdrawalWhenIdempotencyKeyExists() {

    WithdrawalRequest existingRequest =
            WithdrawalRequest.builder()
                    .id(100L)
                    .amount(new BigDecimal("5000"))
                    .status(WithdrawalStatus.PENDING)
                    .idempotencyKey("pending-123")
                    .build();

    when(
            transactionRepository
                    .findByIdempotencyKey(
                            "pending-123"
                    )
    ).thenReturn(Optional.empty());

    when(
            withdrawalRequestRepository
                    .findByIdempotencyKey(
                            "pending-123"
                    )
    ).thenReturn(Optional.of(existingRequest));

    WithdrawRequest request =
            new WithdrawRequest();

    request.setAmount(
            new BigDecimal("5000")
    );

    request.setIdempotencyKey(
            "pending-123"
    );

    TransactionResponse response =
            walletService.withdrawMoney(
                    request
            );

    assertEquals(
            TransactionStatus.PENDING,
            response.getStatus()
    );

    verify(
            withdrawalRequestRepository,
            never()
    ).save(any());

    verify(
            transactionRepository,
            never()
    ).save(any());
}

}
