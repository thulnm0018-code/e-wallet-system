package com.ewallet.backend.service;

import com.ewallet.backend.dto.message.NotificationMessage;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.entity.WithdrawalRequest;
import com.ewallet.backend.enums.AuditAction;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.enums.WithdrawalStatus;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.repository.WithdrawalRequestRepository;
import com.ewallet.backend.service.impl.WithdrawalApprovalServiceImpl;
import com.ewallet.backend.util.TransactionCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalApprovalServiceImplTest {

    @Mock
    private WithdrawalRequestRepository requestRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCodeGenerator codeGenerator;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RabbitProducerService rabbitProducerService;

    @InjectMocks
    private WithdrawalApprovalServiceImpl service;

    private User user;
    private Wallet wallet;
    private WithdrawalRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0987654321");
        user.setUserStatus(UserStatus.ACTIVE);

        wallet = Wallet.builder()
                .id(10L)
                .user(user)
                .balance(new BigDecimal("5000.00"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        request = WithdrawalRequest.builder()
                .id(100L)
                .user(user)
                .amount(new BigDecimal("2000.00"))
                .status(WithdrawalStatus.PENDING)
                .build();
    }

    @SuppressWarnings("null")
    @Test
    void shouldApproveWithdrawal() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));
        when(codeGenerator.generate()).thenReturn("TXN-APPROVED");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.approve(100L);

        verify(transactionRepository).save(any(Transaction.class));
        verify(requestRepository).save(request);
        verify(rabbitProducerService).sendNotification(any(NotificationMessage.class));
        verify(auditLogService).log(eq(user), eq(AuditAction.WITHDRAW_APPROVED), any(String.class));
    }

    @SuppressWarnings("null")
    @Test
    void shouldRejectWithdrawal() {
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

        service.reject(100L);

        verify(requestRepository).save(request);
        verify(rabbitProducerService).sendNotification(any(NotificationMessage.class));
        verify(auditLogService).log(eq(user), eq(AuditAction.WITHDRAW_REJECTED), any(String.class));
    }

    @Test
    void shouldThrowWhenWithdrawalRequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.approve(999L));
    }

    @Test
    void shouldThrowWhenApprovalAlreadyProcessed() {
        request.setStatus(WithdrawalStatus.APPROVED);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class, () -> service.approve(100L));
    }

    @Test
    void shouldThrowWhenBalanceInsufficientForApproval() {
        wallet.setBalance(new BigDecimal("1000.00"));
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));

        assertThrows(BadRequestException.class, () -> service.approve(100L));
    }
}
