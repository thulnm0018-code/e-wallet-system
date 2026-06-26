package com.ewallet.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {
    @NotBlank(message = "Receiver phone number is required")
    private String receiverPhone;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum transaction is 1,000 VND")
    private BigDecimal amount;
 
    @Size(
    max = 255,
    message = "Message cannot exceed 255 characters"
    )
    private String message;
}