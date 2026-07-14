package com.ewallet.backend.controller;

import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.SuspiciousActivityResponse;
import com.ewallet.backend.service.SuspiciousActivityService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suspicious-activities")
public class SuspiciousActivityController {

    private final SuspiciousActivityService
            suspiciousActivityService;

    public SuspiciousActivityController(
            SuspiciousActivityService suspiciousActivityService
    ) {
        this.suspiciousActivityService =
                suspiciousActivityService;
    }

    @GetMapping
    public ApiResponse<
            List<SuspiciousActivityResponse>>
    getAllActivities() {

        return ApiResponse
                .<List<SuspiciousActivityResponse>>
                        builder()
                .data(
                        suspiciousActivityService
                                .getAllActivities()
                )
                .build();
    }
}