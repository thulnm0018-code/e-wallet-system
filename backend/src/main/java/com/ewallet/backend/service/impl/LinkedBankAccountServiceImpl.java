package com.ewallet.backend.service.impl;

import com.ewallet.backend.dto.request.LinkBankAccountRequest;
import com.ewallet.backend.dto.response.LinkedBankAccountResponse;
import com.ewallet.backend.entity.LinkedBankAccount;
import com.ewallet.backend.entity.User;
import com.ewallet.backend.exception.BadRequestException;
import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.repository.LinkedBankAccountRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.security.service.CurrentUserService;
import com.ewallet.backend.service.LinkedBankAccountService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LinkedBankAccountServiceImpl
        implements LinkedBankAccountService {

    private final LinkedBankAccountRepository linkedBankAccountRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public LinkedBankAccountServiceImpl(
            LinkedBankAccountRepository linkedBankAccountRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService
    ) {
        this.linkedBankAccountRepository = linkedBankAccountRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @SuppressWarnings("null")
    @Override
    public void linkBankAccount(
            LinkBankAccountRequest request
    ) {

        Long userId = currentUserService.getCurrentUserId();

        if (linkedBankAccountRepository
                .existsByUser_IdAndAccountNumber(
                        userId,
                        request.getAccountNumber()
                )) {

            throw new BadRequestException(
                    "Bank account already linked"
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new NotFoundException("User not found")
                );

        LinkedBankAccount account =
                LinkedBankAccount.builder()
                        .bankName(request.getBankName())
                        .accountNumber(request.getAccountNumber())
                        .accountHolderName(
                                request.getAccountHolderName()
                        )
                        .linkedAt(LocalDateTime.now())
                        .user(user)
                        .build();

        linkedBankAccountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkedBankAccountResponse>
    getMyLinkedAccounts() {

        Long userId = currentUserService.getCurrentUserId();

        return linkedBankAccountRepository
                .findByUser_Id(userId)
                .stream()
                .map(account ->
                        LinkedBankAccountResponse.builder()
                                .id(account.getId())
                                .bankName(account.getBankName())
                                .accountNumber(account.getAccountNumber())
                                .accountHolderName(
                                        account.getAccountHolderName()
                                )
                                .linkedAt(account.getLinkedAt())
                                .build()
                )
                .toList();
    }

    @SuppressWarnings("null")
    @Override
    public void unlinkAccount(Long accountId) {

        Long userId = currentUserService.getCurrentUserId();

        LinkedBankAccount account =
                linkedBankAccountRepository
                        .findByIdAndUser_Id(
                                accountId,
                                userId
                        )
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Linked bank account not found"
                                )
                        );

        linkedBankAccountRepository.delete(account);
    }
}