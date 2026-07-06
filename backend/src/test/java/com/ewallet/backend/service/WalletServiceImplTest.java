package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.WalletResponse;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.mapper.TransactionMapper;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.security.CurrentUserService;
import com.ewallet.backend.service.impl.WalletServiceImpl;
import com.ewallet.backend.util.TransactionCodeGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void getMyWalletShouldReturnCurrentUserWalletInfo() {
        User user = new User();
        user.setId(1L);
        user.setPhone("0987654321");
        user.setName("Test User");
        user.setUserStatus(UserStatus.ACTIVE);

        Wallet wallet = Wallet.builder()
                .id(10L)
                .user(user)
                .balance(new BigDecimal("125.50"))
                .walletStatus(WalletStatus.ACTIVE)
                .build();

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(walletRepository.findByUser_Id(1L)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getMyWallet();

        assertEquals(10L, response.getId());
        assertEquals("125.50", response.getBalance().toPlainString());
        assertEquals("ACTIVE", response.getWalletStatus());
        assertEquals("0987654321", response.getPhone());
    }
}
