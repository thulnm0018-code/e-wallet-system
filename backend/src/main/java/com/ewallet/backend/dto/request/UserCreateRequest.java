package com.ewallet.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String name;
    private String email;
    private String phone;
    private String password; // ng dung se nhap vao mat khau, backend se hash truoc khi luu vao database
}
