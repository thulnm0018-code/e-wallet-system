package com.ewallet.backend.controller;

import com.ewallet.backend.exception.NotFoundException;
import com.ewallet.backend.dto.request.NotificationDispatchRequest;
import com.ewallet.backend.dto.response.AdminDashboardResponse;
import com.ewallet.backend.dto.response.AdminDashboardStatsResponse;
import com.ewallet.backend.dto.response.AdminUserResponse;
import com.ewallet.backend.dto.response.ApiResponse;
import com.ewallet.backend.dto.response.MonthlyStatisticResponse;
import com.ewallet.backend.enums.UserStatus;
import com.ewallet.backend.service.AdminService;
import com.ewallet.backend.repository.TransactionRepository;
import com.ewallet.backend.repository.UserRepository;
import com.ewallet.backend.repository.WalletRepository;
import com.ewallet.backend.service.RabbitProducerService;
import com.ewallet.backend.service.AuditLogService;
import com.ewallet.backend.entity.Transaction;
import com.ewallet.backend.dto.response.AdminTransactionResponse;
import com.ewallet.backend.enums.TransactionStatus;
import com.ewallet.backend.dto.message.NotificationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

        private final AdminService adminService;
        private final TransactionRepository transactionRepository;
        private final RabbitProducerService rabbitProducerService;
        private final AuditLogService auditLogService;

        public AdminController(AdminService adminService,
                                                   TransactionRepository transactionRepository,
                                                   UserRepository userRepository,
                                                   WalletRepository walletRepository,
                                                   RabbitProducerService rabbitProducerService,
                                                   AuditLogService auditLogService) {
                this.adminService = adminService;
                this.transactionRepository = transactionRepository;
                this.rabbitProducerService = rabbitProducerService;
                this.auditLogService = auditLogService;
        }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {

        return ResponseEntity.ok(
                ApiResponse.<AdminDashboardResponse>builder()
                        .message("Dashboard statistics fetched successfully")
                        .data(
                                adminService.getDashboard()
                        )
                        .build()
        );
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats() {

        AdminDashboardResponse core = adminService.getDashboard();

        // Simulated infrastructure telemetry
        AdminDashboardStatsResponse stats = AdminDashboardStatsResponse.builder()
                .dashboard(core)
                .redisTelemetry(AdminDashboardStatsResponse.RedisTelemetry.builder()
                        .hitRate(0.95)
                        .memoryMb(128)
                        .connectedClients(10)
                        .build())
                .rabbitTelemetry(AdminDashboardStatsResponse.RabbitTelemetry.builder()
                        .incomingRate(12)
                        .ready(5)
                        .unacked(0)
                        .activeConsumers(2)
                        .build())
                .deployments(AdminDashboardStatsResponse.DeploymentsStatus.builder()
                        .apiGateway("UP")
                        .authService("UP")
                        .database("UP")
                        .rabbitmq("UP")
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.<AdminDashboardStatsResponse>builder()
                .message("Dashboard stats fetched")
                .data(stats)
                .build());
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<AdminTransactionResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> p;
        if (status == null || status.isBlank()) {
            p = transactionRepository.findAllWithUsers(pageable);
        } else {
            p = transactionRepository.findAllWithUsersAndStatus(
                    com.ewallet.backend.enums.TransactionStatus.valueOf(status),
                    pageable
            );
        }
        Page<AdminTransactionResponse> mapped = p.map(AdminTransactionResponse::fromEntity);
        return ResponseEntity.ok(ApiResponse.<Page<AdminTransactionResponse>>builder()
                .message("Transactions fetched")
                .data(mapped)
                .build());
    }

    @PostMapping("/transactions/{transactionId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveDepositRequest(
            @PathVariable Long transactionId
    ) {
        adminService.approveDepositRequest(transactionId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Deposit request approved").build());
    }

    @PostMapping("/transactions/{transactionId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectDepositRequest(
            @PathVariable Long transactionId
    ) {
        adminService.rejectDepositRequest(transactionId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Deposit request rejected").build());
    }

    @GetMapping("/monitoring/suspicious")
    public ResponseEntity<ApiResponse<List<AdminTransactionResponse>>> getSuspiciousTransactions() {
        // For now, reuse transactions with status PENDING as suspicious
        // Consider FAILED transactions as suspicious instead of relying on PENDING
        List<Transaction> list = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.FAILED)
                .toList();
        List<AdminTransactionResponse> resp = list.stream().map(AdminTransactionResponse::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.<List<AdminTransactionResponse>>builder().message("Suspicious txns").data(resp).build());
    }

    @PostMapping("/monitoring/suspicious/{txnRef}/approve")
    public ResponseEntity<ApiResponse<Void>> approveSuspicious(@PathVariable String txnRef) {
        Transaction tx = transactionRepository.findByTransactionCode(txnRef)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(tx);
        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Transaction approved").build());
    }

    @PostMapping("/monitoring/suspicious/{txnRef}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectSuspicious(@PathVariable String txnRef) {
        Transaction tx = transactionRepository.findByTransactionCode(txnRef)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        tx.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(tx);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.<Void>builder().message("Transaction rejected").build());
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<com.ewallet.backend.dto.response.AuditLogResponse>>> getAdminLogs() {
        return ResponseEntity.ok(ApiResponse.<List<com.ewallet.backend.dto.response.AuditLogResponse>>builder()
                .message("Logs fetched")
                .data(auditLogService.getAllLogs())
                .build());
    }

    @PostMapping("/notifications/dispatch")
    public ResponseEntity<ApiResponse<Void>> dispatchNotification(@RequestBody NotificationDispatchRequest request) {
        // Dispatch a simple notification message to RabbitMQ (non-fatal)
        rabbitProducerService.sendNotification(NotificationMessage.builder()
                .userId(null)
                .title(request.getEventType())
                .content("Simulated event: " + request.getEventType())
                .build());

        return ResponseEntity.ok(ApiResponse.<Void>builder().message("Dispatched").build());
    }

    @GetMapping("/dashboard/monthly")
public ResponseEntity<ApiResponse<List<MonthlyStatisticResponse>>>
getMonthlyStatistics() {

    return ResponseEntity.ok(
            ApiResponse.<List<MonthlyStatisticResponse>>builder()
                    .message("Monthly statistics fetched successfully")
                    .data(
                            adminService.getMonthlyStatistics()
                    )
                    .build()
    );
}

@GetMapping("/users")
public ResponseEntity<
        ApiResponse<Page<AdminUserResponse>>
        > searchUsers(

        @RequestParam(required = false)
        String keyword,

        @RequestParam(required = false)
        UserStatus status,

        @RequestParam(defaultValue = "0")
        int page,

        @RequestParam(defaultValue = "10")
        int size
) {

    Pageable pageable =
            PageRequest.of(page, size);

    return ResponseEntity.ok(
            ApiResponse.<Page<AdminUserResponse>>builder()
                    .message(
                            "Users fetched successfully"
                    )
                    .data(
                            adminService.searchUsers(
                                    keyword,
                                    status,
                                    pageable
                            )
                    )
                    .build()
    );
}

    @DeleteMapping("/users/{userId}")
public ResponseEntity<ApiResponse<Void>> deleteUser(
        @PathVariable Long userId
) {

    adminService.deleteUser(userId);

    return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                    .message("User deleted successfully")
                    .build()
    );
}

@PatchMapping("/users/{userId}/restore")
public ResponseEntity<ApiResponse<Void>> restoreUser(
        @PathVariable Long userId
) {

    adminService.restoreUser(userId);

    return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                    .message("User restored successfully")
                    .build()
    );
}

}