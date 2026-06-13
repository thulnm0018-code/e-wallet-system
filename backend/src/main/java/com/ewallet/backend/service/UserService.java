package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.UserCreateRequest;

public interface UserService {
    void registerUser(UserCreateRequest request);
}