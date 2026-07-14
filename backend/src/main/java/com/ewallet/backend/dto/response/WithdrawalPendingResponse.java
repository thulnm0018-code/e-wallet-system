package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WithdrawalPendingResponse {

    private String message;

    private Long requestId;
}