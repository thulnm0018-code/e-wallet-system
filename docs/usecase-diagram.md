# Lược đồ Use Case - Hệ thống E-Wallet

```mermaid
flowchart LR

    User([User])
    Admin([Admin])

    subgraph System["E-Wallet System"]

        UC1([Đăng ký tài khoản])
        UC2([Đăng nhập])
        UC3([Quên mật khẩu])
        UC4([Quản lý hồ sơ])
        UC5([Xem số dư])
        UC6([Chuyển tiền])
        UC7([Nạp tiền])
        UC8([Rút tiền])
        UC9([Xem lịch sử giao dịch])
        UC10([Xem thông báo])
        UC11([Quản lý tài khoản ngân hàng liên kết])

        OTP([Xác thực OTP])

        AC1([Xem Dashboard])
        AC2([Quản lý người dùng])
        AC3([Quản lý giao dịch])
        AC4([Xem Audit Log])
        AC5([Phê duyệt giao dịch rút tiền giá trị cao])
        AC6([Xem doanh thu phí dịch vụ])

    end

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

    UC1 -. include .-> OTP
    UC6 -. include .-> OTP
```