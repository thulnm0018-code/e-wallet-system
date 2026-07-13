package com.ewallet.backend.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkedBankAccountResponse {

    private Long id;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private LocalDateTime linkedAt;
}