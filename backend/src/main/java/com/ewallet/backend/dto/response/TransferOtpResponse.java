package com.ewallet.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferOtpResponse {
    private String message;
    private String receiverName;
    private String receiverPhone;
    private String amount;
    private Long expiresIn;
}
