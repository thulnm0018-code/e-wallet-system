package com.ewallet.backend.service;

import com.ewallet.backend.dto.request.ForgotPasswordRequest;
import com.ewallet.backend.dto.request.ResetPasswordRequest;
import com.ewallet.backend.dto.request.UpdateProfileRequest;
import com.ewallet.backend.dto.request.UserCreateRequest;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminTransactionResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.ReceiverLookupResponse;
import com.ewallet.backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse registerUser(UserCreateRequest request);

    ReceiverLookupResponse getUserByPhone(String phone);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String currentPassword, String newPassword);

    UserResponse updateProfile(UpdateProfileRequest request);

    List<AdminUserResponse> getAdminUsers();

    List<AdminTransactionResponse> getAdminTransactions();

    AdminDashboardResponse getAdminDashboard();
}