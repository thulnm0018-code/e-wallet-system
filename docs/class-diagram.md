# Lược đồ Lớp - Hệ thống E-Wallet

```mermaid
classDiagram
    class User {
        +Long id
        +String name
        +String phone
        +String email
        +String password
        +Role role
        +UserStatus status
        +String address
        +LocalDate dateOfBirth
    }

    class Wallet {
        +Long id
        +Long userId
        +BigDecimal balance
        +WalletStatus status
    }

    class Transaction {
        +Long id
        +Long senderWalletId
        +Long receiverWalletId
        +TransactionType type
        +BigDecimal amount
        +TransactionStatus status
        +String message
        +String transactionCode
        +String idempotencyKey
    }

    class OTP {
        +Long id
        +Long userId
        +String otpCode
        +LocalDateTime expiredAt
        +Boolean verified
    }

    class Notification {
        +Long id
        +Long userId
        +String title
        +String content
        +Boolean isRead
        +LocalDateTime createdAt
    }

    class RefreshToken {
        +Long id
        +Long userId
        +String token
        +LocalDateTime expiry
    }

    class AuditLog {
        +Long id
        +Long userId
        +String action
        +String description
        +LocalDateTime createdAt
    }

    class LinkedBankAccount {
        +Long id
        +String bankName
        +String accountNumber
        +String accountHolderName
        +LocalDateTime linkedAt
    }

    class WithdrawalRequest {
        +Long id
        +Long userId
        +BigDecimal amount
        +WithdrawalStatus status
        +String idempotencyKey
        +LocalDateTime createdAt
        +LocalDateTime approvedAt
        +LocalDateTime rejectedAt
    }

    class Role {
        <<enumeration>>
        USER
        ADMIN
    }

    class UserStatus {
        <<enumeration>>
        ACTIVE
        LOCKED
        PENDING
        DELETED
    }

    class WalletStatus {
        <<enumeration>>
        ACTIVE
        LOCKED
        PENDING
    }

    class TransactionType {
        <<enumeration>>
        TRANSFER
        DEPOSIT
        WITHDRAW
        DEPOSIT_REQUEST
    }

    class TransactionStatus {
        <<enumeration>>
        PENDING
        SUCCESS
        FAILED
        REJECTED
    }

    class WithdrawalStatus {
        <<enumeration>>
        PENDING
        APPROVED
        REJECTED
    }

    User "1" --> "1" Wallet : owns
    User "1" --> "0..*" Notification : receives
    User "1" --> "0..*" RefreshToken : has
    User "1" --> "0..*" OTP : requests
    User "1" --> "0..*" AuditLog : triggers
    User "1" --> "0..*" Transaction : performs
    User "1" --> "0..*" LinkedBankAccount : links
    User "1" --> "0..*" WithdrawalRequest : requests

    Wallet "1" --> "0..*" Transaction : sends
    Wallet "1" --> "0..*" Transaction : receives
    WithdrawalRequest "*" --> "1" User : requests
