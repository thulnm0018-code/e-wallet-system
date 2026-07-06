package com.ewallet.backend.service;

import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.dto.response.WalletResponse;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.mapper.TransactionMapper;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.impl.WalletServiceImpl;
import com.ewallet.backend.util.TransactionCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        WalletResponse response = walletService.getMyWallet();

        verify(currentUserService).getCurrentUserId();
        verify(walletRepository).findByUser_Id(1L);

        assertEquals(10L, response.getId());
        assertEquals("125.50", response.getBalance().toPlainString());
        assertEquals("ACTIVE", response.getWalletStatus());
        assertEquals("Test User",response.getName());
        assertEquals("0987654321", response.getPhone());
    }

    @Test
    void getMyWalletShouldThrowWhenWalletNotFound() {

    when(currentUserService.getCurrentUserId()).thenReturn(1L);
    when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

    NotFoundException exception =assertThrows(
                NotFoundException.class,
                () -> walletService.getMyWallet());

    assertEquals("Wallet not found",exception.getMessage());

    verify(currentUserService).getCurrentUserId();
    verify(walletRepository).findByUser_Id(1L);
   }
}
