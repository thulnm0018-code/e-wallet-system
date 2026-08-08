# Lược đồ Use Case - Hệ thống E-Wallet

```mermaid
usecaseDiagram
  left to right direction

  actor User
  actor Admin

  usecase "Đăng ký tài khoản" as UC1
  usecase "Đăng nhập" as UC2
  usecase "Quên mật khẩu" as UC3
  usecase "Quản lý hồ sơ" as UC4
  usecase "Xem số dư" as UC5
  usecase "Chuyển tiền" as UC6
  usecase "Nạp tiền" as UC7
  usecase "Rút tiền" as UC8
  usecase "Xem lịch sử giao dịch" as UC9
  usecase "Xem thông báo" as UC10
  usecase "Quản lý tài khoản ngân hàng liên kết" as UC11
  usecase "Xác thực OTP" as OTP

  usecase "Xem Dashboard" as AC1
  usecase "Quản lý người dùng" as AC2
  usecase "Quản lý giao dịch" as AC3
  usecase "Xem Audit Log" as AC4
  usecase "Phê duyệt giao dịch rút tiền giá trị cao" as AC5
  usecase "Xem doanh thu phí dịch vụ" as AC6

  User --> UC1
  User --> UC2
  User --> UC3
  User --> UC4
  User --> UC5
  User --> UC6
  User --> UC7
  User --> UC8
  User --> UC9
  User --> UC10
  User --> UC11

  Admin --> AC1
  Admin --> AC2
  Admin --> AC3
  Admin --> AC4
  Admin --> AC5
  Admin --> AC6

  UC1 ..> OTP : <<include>>
  UC6 ..> OTP : <<include>>
```
