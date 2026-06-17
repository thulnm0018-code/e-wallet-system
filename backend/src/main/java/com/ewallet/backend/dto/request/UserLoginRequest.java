package com.ewallet.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "Email or Phone cannot be blank")
    private String identifier;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}