package com.ewallet.backend.service;

import com.ewallet.backend.dto.response.UserResponse;
import com.ewallet.backend.dto.request.UserCreateRequest;



public interface UserService {

    UserResponse registerUser(UserCreateRequest request);

   
}