package com.ewallet.backend.dto.request;

import com.ewallet.backend.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSearchRequest {

    private String keyword;

    private UserStatus status;
}