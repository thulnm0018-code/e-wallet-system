package com.ewallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be at least 8 characters long")
    private String newPassword;
}
