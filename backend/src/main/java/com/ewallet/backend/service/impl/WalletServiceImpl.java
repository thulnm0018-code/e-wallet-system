package com.ewallet.backend.service.impl;

import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ewallet.backend.exception.ResourceConflictException;
import com.ewallet.backend.dto.request.DepositRequest;
import com.ewallet.backend.dto.request.TransferRequest;
import com.ewallet.backend.dto.request.TransferInitiateRequest;
import com.ewallet.backend.dto.request.WithdrawRequest;
import com.ewallet.backend.dto.response.TransactionResponse;
import com.ewallet.backend.dto.response.TransferOtpResponse;
import com.ewallet.backend.dto.response.WalletResponse;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.entity.Wallet;
import com.ewallet.backend.entity.Otp;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.enums.TransactionType;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.enums.WalletStatus;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.repository.OtpRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.WalletService;
import com.ewallet.backend.util.PhoneUtils;
import com.ewallet.backend.util.TransactionCodeGenerator;
import com.ewallet.backend.util.OtpUtils;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.ForbiddenException;
import com.ewallet.backend.exception.AccountInactiveException;
import com.ewallet.backend.mapper.TransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCodeGenerator codeGenerator;
    private final CurrentUserService currentUserService;
    private final TransactionMapper transactionMapper;
    private final OtpRepository otpRepository;

    private static final BigDecimal MIN_TRANSFER_AMOUNT =new BigDecimal("1.00");
    private static final BigDecimal MAX_TRANSFER_AMOUNT =new BigDecimal("5000.00");

    private final ConcurrentHashMap<String, Boolean>processingTransfers = new ConcurrentHashMap<>();


    public WalletServiceImpl(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            TransactionCodeGenerator codeGenerator,
            CurrentUserService currentUserService,
            TransactionMapper transactionMapper,
            OtpRepository otpRepository) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.codeGenerator = codeGenerator;
        this.currentUserService = currentUserService;
        this.transactionMapper = transactionMapper;
        this.otpRepository = otpRepository;
  
    }

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    @Override
    @Transactional
    public TransferOtpResponse initiateTransfer(TransferInitiateRequest request) {
        Long senderUserId = currentUserService.getCurrentUserId();
        log.info("[TRANSFER-INITIATE] START - UserId: {}", senderUserId);

        validateAmount(request.getAmount());
        validateTransferLimits(request.getAmount());

        String receiverPhone = PhoneUtils.normalize(request.getReceiverPhone());
        if (receiverPhone == null) {
            throw new BadRequestException("Invalid receiver phone number");
        }
        log.info("[TRANSFER-INITIATE] Normalized receiver phone: {}", receiverPhone);

        Wallet senderWalletTemp = getWalletByUserId(senderUserId);
        log.info("[TRANSFER-INITIATE] Sender wallet found: {}", senderWalletTemp.getId());

        Wallet receiverWalletTemp = walletRepository
                .findByUser_Phone(receiverPhone)
                .orElseThrow(() -> new NotFoundException("Receiver wallet not found"));

        if (senderWalletTemp.getId().equals(receiverWalletTemp.getId())) {
            throw new BadRequestException("Cannot transfer money to yourself");
        }

        validateActiveWallet(senderWalletTemp, "Sender wallet");
        validateActiveWallet(receiverWalletTemp, "Receiver wallet");
        validateUserStatus(senderWalletTemp.getUser());
        validateUserStatus(receiverWalletTemp.getUser());

        if (senderWalletTemp.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        // Invalidate any existing OTPs before generating a new one
        otpRepository.deleteAllByUser(senderWalletTemp.getUser());

        // Generate OTP for this transfer and bind it to amount + receiverPhone
        String otpCode = OtpUtils.generateOtp();
        Wallet senderWallet = senderWalletTemp;
        String phone = senderWallet.getUser().getPhone();
        log.info("[TRANSFER-INITIATE] OTP generated for user {}", phone);
        log.debug("[TRANSFER-INITIATE] OTP code for user {}: {}", phone, otpCode);

        Otp otp = Otp.builder()
            .user(senderWallet.getUser())
            .otpCode(otpCode)
            .verified(false)
            .expiredAt(LocalDateTime.now().plusMinutes(5))
            .amount(request.getAmount())
            .receiverPhone(receiverPhone)
            .build();

        otpRepository.save(Objects.requireNonNull(otp));
        
        log.info("[TRANSFER-INITIATE] OTP saved to database for user {}", senderWallet.getUser().getPhone());

        log.info("==========================================");
        log.info("MA OTP CHUYEN TIEN CHO [{}]: {}", senderWallet.getUser().getPhone(), otpCode);
        log.info("SO TIEN: {}", request.getAmount());
        log.info("NGUOI NHAN: {}", receiverWalletTemp.getUser().getPhone());
        log.info("HAN SD: 5 PHUT");
        log.info("==========================================");

        TransferOtpResponse response = TransferOtpResponse.builder()
                .message("OTP generated successfully. Check console for OTP code.")
                .receiverName(receiverWalletTemp.getUser().getName())
                .receiverPhone(receiverPhone)
                .amount(request.getAmount().toString())
                .expiresIn(300L)
                .build();
        
        log.info("[TRANSFER-INITIATE] Response prepared: {}", response.getMessage());
        log.info("[TRANSFER-INITIATE] COMPLETE");
        
        return response;
    }

    @Override
    @Transactional
    public TransactionResponse transferMoney(TransferRequest request) {
        Long senderUserId = currentUserService.getCurrentUserId();

        validateAmount(request.getAmount());
        validateTransferLimits(request.getAmount());

        String transferKey = senderUserId + "-" + request.getAmount() + "-" + request.getReceiverPhone();

        if (processingTransfers.putIfAbsent(transferKey,Boolean.TRUE) != null) {

        throw new ResourceConflictException("Transfer already being processed");
}

try {
        // Validate OTP
        if (request.getOtpCode() == null || request.getOtpCode().isBlank()) {
            throw new BadRequestException("OTP code is required");
        }

        Wallet senderWalletTemp = getWalletByUserId(senderUserId);
        validateUserStatus(senderWalletTemp.getUser());

        Otp otp = otpRepository
            .findTopByUserOrderByCreatedAtDesc(senderWalletTemp.getUser())
            .orElseThrow(() -> new BadRequestException("No OTP found. Please initiate transfer first."));

        // Ensure OTP was generated for this exact transfer (amount + receiver)
        String receiverPhone = PhoneUtils.normalize(request.getReceiverPhone());
        if (receiverPhone == null) {
            throw new BadRequestException("Invalid receiver phone number");
        }

        if (otp.getReceiverPhone() == null || !otp.getReceiverPhone().equals(receiverPhone)
            || otp.getAmount() == null || otp.getAmount().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException("OTP does not match transfer details");
        }

        if (otp.isVerified()) {
            throw new BadRequestException("OTP already used");
        }

        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            Integer attempts = otp.getFailedAttempts() == null ? 0 : otp.getFailedAttempts();
            attempts++;
            otp.setFailedAttempts(attempts);

            if (attempts >= 5) {
                otp.setVerified(true);
                otpRepository.save(otp);
                log.warn("OTP locked for user {} after {} failed attempts", senderWalletTemp.getUser().getPhone(), attempts);
                throw new BadRequestException("OTP locked. Please request a new OTP.");
            }

            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP");
        }

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }

        Wallet receiverWalletTemp = walletRepository
                .findByUser_Phone(receiverPhone)
                .orElseThrow(() -> new NotFoundException("Receiver wallet not found"));

        if (senderWalletTemp.getId().equals(receiverWalletTemp.getId())) {
            throw new BadRequestException("Cannot transfer money to yourself");
        }

        Long firstWalletId = Math.min(senderWalletTemp.getId(), receiverWalletTemp.getId());
        Long secondWalletId = Math.max(senderWalletTemp.getId(), receiverWalletTemp.getId());

        Wallet firstLockedWallet = lockWallet(firstWalletId);
        Wallet secondLockedWallet = lockWallet(secondWalletId);

        Wallet senderWallet = firstLockedWallet.getId().equals(senderWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        Wallet receiverWallet = firstLockedWallet.getId().equals(receiverWalletTemp.getId()) 
                ? firstLockedWallet 
                : secondLockedWallet;

        
        validateActiveWallet(senderWallet, "Sender wallet");
        validateActiveWallet(receiverWallet, "Receiver wallet");
        validateUserStatus(senderWallet.getUser());
        validateUserStatus(receiverWallet.getUser());

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));
    
    walletRepository.saveAll(Objects.requireNonNull(List.of(senderWallet, receiverWallet)));


        Transaction transaction = Transaction.builder()
                
                .transactionCode(codeGenerator.generate())
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(request.getAmount())
                .message(request.getMessage() == null || request.getMessage().isBlank() 
                        ? "Transfer" 
                        : request.getMessage())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        Transaction savedTransaction =
        transactionRepository.save(
                Objects.requireNonNull(transaction)
        );

        otp.setFailedAttempts(0);
        otp.setVerified(true);
        otpRepository.save(otp);

        return transactionMapper.toResponse(savedTransaction);
    }
    
finally {

        processingTransfers.remove(
                transferKey);
    }
}

    @Override
    @Transactional
    public TransactionResponse depositMoney(DepositRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        validateAmount(request.getAmount());

        Wallet wallet = getWalletByUserId(userId);
        Wallet lockedWallet = lockWallet(wallet.getId());

    
        validateActiveWallet(lockedWallet, "Wallet");
        validateUserStatus(lockedWallet.getUser());

        lockedWallet.setBalance(lockedWallet.getBalance().add(request.getAmount()));
    walletRepository.save(lockedWallet);

    Transaction transaction = Transaction.builder()
            .transactionCode(codeGenerator.generate())
            .senderWallet(null)
            .receiverWallet(lockedWallet)
            .amount(request.getAmount())
            .message(request.getMessage() == null
                        || request.getMessage().isBlank()
                        ? "Deposit money"
                        : request.getMessage())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();

    Transaction savedTransaction =
            transactionRepository.save(
                    Objects.requireNonNull(transaction)
            );

    return transactionMapper.toResponse(savedTransaction);
}
    
    @Override
    @Transactional
    public TransactionResponse withdrawMoney(WithdrawRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        validateAmount(request.getAmount());

        Wallet wallet = getWalletByUserId(userId);

      Wallet lockedWallet = lockWallet(wallet.getId());

        validateActiveWallet(lockedWallet, "Wallet");
        validateUserStatus(lockedWallet.getUser());

        if (lockedWallet.getBalance().compareTo(request.getAmount()) < 0) {

        throw new BadRequestException(
                "Insufficient balance"
        );
    }

    lockedWallet.setBalance(lockedWallet.getBalance()
                .subtract(request.getAmount()));

    walletRepository.save(lockedWallet);

    Transaction transaction = Transaction.builder()
            .transactionCode(
                    codeGenerator.generate()
            )

            .senderWallet(lockedWallet)

            .receiverWallet(null)

            .amount(request.getAmount())

            .message(
                    request.getMessage() == null
                            || request.getMessage().isBlank()
                            ? "Withdraw money"
                            : request.getMessage()
            )

            .type(TransactionType.WITHDRAW)
            .status(TransactionStatus.SUCCESS)
            .build();

    Transaction savedTransaction =
            transactionRepository.save(
                    Objects.requireNonNull(transaction)
            );

    return transactionMapper.toResponse(
            savedTransaction
    );
}

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory() {
        return getMyHistory(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory(String type, String startDate, String endDate) {
        return getMyHistory(type, startDate, endDate, 0, 50);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyHistory(String type, String startDate, String endDate, Integer page, Integer size) {
        Long userId = currentUserService.getCurrentUserId();
        Wallet wallet = getWalletByUserId(userId);

        TransactionType transactionType = null;
        if (type != null && !type.isBlank()) {
            try {
                transactionType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid transaction type");
            }
        }

        LocalDateTime start = parseDate(startDate, true);
        LocalDateTime end = parseDate(endDate, false);

        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : Math.min(size, 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        return transactionRepository
                .findWalletTransactions(wallet.getId(), transactionType, start, end, pageable)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyWallet() {
        Long userId = currentUserService.getCurrentUserId();
        Wallet wallet = getWalletByUserId(userId);

        return WalletResponse.builder()
                .id(wallet.getId())
                .phone(wallet.getUser().getPhone())
                .name(wallet.getUser().getName())
                .balance(wallet.getBalance())
                .walletStatus(wallet.getWalletStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMyBalance() {
        Long userId = currentUserService.getCurrentUserId();

       Wallet wallet = getWalletByUserId(userId);

        return wallet.getBalance();
    }

        private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }
    }

        private void validateTransferLimits(
        BigDecimal amount) {

    if (amount.compareTo(
            MIN_TRANSFER_AMOUNT) < 0) {

        throw new BadRequestException(
                "Transfer amount is below minimum limit");
    }

    if (amount.compareTo(
            MAX_TRANSFER_AMOUNT) > 0) {

        throw new BadRequestException(
                "Transfer amount exceeds maximum limit");
    }
}

    private Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
    }

    private void validateActiveWallet(Wallet wallet, String walletName) {
        if (wallet.getWalletStatus() != WalletStatus.ACTIVE) {
            throw new BadRequestException(walletName + " is inactive");
        }
    }

        private void validateUserStatus(User user) {

        if (user == null) {
                throw new NotFoundException("User not found");
        }

        UserStatus status = user.getUserStatus();

        if (status == UserStatus.ACTIVE) {
                return;
        }

        if (status == UserStatus.LOCKED) {
                throw new AccountInactiveException(
                        "User status is LOCKED"
                );
        }

        throw new BadRequestException(
                "User status is " + status
        );
}
         
    private LocalDateTime parseDate(String value, boolean startOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }

        LocalDate date = LocalDate.parse(value);
        return startOfDay ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
    }

private Wallet lockWallet(Long walletId) {

        try {return walletRepository
                        .findByIdForUpdate(walletId)
                        .orElseThrow(() ->
                new NotFoundException("Wallet not found"));
        } 

        catch (PessimisticLockingFailureException | LockTimeoutException ex) {

                throw new ResourceConflictException(
                        "Wallet is currently being processed. Please try again later.");}
        }

        @Override
@Transactional(readOnly = true)
public TransactionResponse getTransactionDetail(Long id) {

    Long currentUserId = currentUserService.getCurrentUserId();

    @SuppressWarnings("null")
    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() ->
                    new NotFoundException("Transaction not found"));

    boolean sender = transaction.getSenderWallet() != null
            && transaction.getSenderWallet()
            .getUser()
            .getId()
            .equals(currentUserId);

    boolean receiver = transaction.getReceiverWallet() != null
            && transaction.getReceiverWallet()
            .getUser()
            .getId()
            .equals(currentUserId);

    if (!sender && !receiver) {
        throw new ForbiddenException(
                "You are not authorized to view this transaction");
    }

    return transactionMapper.toResponse(transaction);
}
}