# Lược đồ Lớp - Hệ thống E-Wallet

```mermaid
classDiagram
    class User {
        +Long id
        +String name
        +String phone
        +String email
        +String password
        +String role
        +register()
        +login()
        +updateProfile()
        +changePassword()
    }

    class Wallet {
        +Long id
        +Long userId
        +BigDecimal balance
        +String status
        +getBalance()
        +deposit()
        +withdraw()
    }

    class Transaction {
        +Long id
        +Long userId
        +String type
        +BigDecimal amount
        +String status
        +String message
        +String transactionCode
        +createTransaction()
    }

    class OTP {
        +Long id
        +String identifier
        +String otpCode
        +LocalDateTime expiry
        +generateOTP()
        +verifyOTP()
    }

    class Notification {
        +Long id
        +Long userId
        +String title
        +String content
        +Boolean isRead
        +markAsRead()
    }

    class RefreshToken {
        +Long id
        +Long userId
        +String token
        +LocalDateTime expiry
        +generateToken()
    }

    class AuditLog {
        +Long id
        +String action
        +String actor
        +String target
        +LocalDateTime createdAt
    }

    class AuthController {
        +login()
        +register()
        +verifyOtp()
        +logout()
    }

    class WalletController {
        +transfer()
        +deposit()
        +withdraw()
        +getHistory()
    }

    class UserController {
        +updateProfile()
        +changePassword()
        +uploadAvatar()
    }

    class AdminController {
        +getDashboard()
        +manageUsers()
        +manageTransactions()
        +viewLogs()
    }

    User "1" --> "1" Wallet : owns
    User "1" --> "0..*" Transaction : performs
    User "1" --> "0..*" Notification : receives
    User "1" --> "0..*" RefreshToken : has
    User "1" --> "0..*" OTP : uses
    User "1" --> "0..*" AuditLog : triggers

    Wallet "1" --> "0..*" Transaction : records
