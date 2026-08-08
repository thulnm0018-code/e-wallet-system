# TÀI LIỆU ĐẶC TẢ HỆ THỐNG (SYSTEM REQUIREMENT SPECIFICATION)

## 1. Giới thiệu Tổng quan (Overview)

### 1.1. Mục đích

Tài liệu này đặc tả chi tiết các yêu cầu chức năng, phi chức năng, kiến trúc hệ thống và thiết kế dữ liệu cho dự án hệ thống ví điện tử. Hệ thống giúp người dùng quản lý tài khoản, theo dõi số dư, thực hiện chuyển tiền và giám sát hoạt động doanh thu thông qua giao diện quản trị dành cho admin.

### 1.2. Phạm vi hệ thống (Scope)

- Đối tượng sử dụng:
  - Người dùng cuối (User/Customer)
  - Quản trị viên (Admin)
  - Hệ thống và dịch vụ bên ngoài (Database, RabbitMQ, JWT, Docker)
- Môi trường triển khai:
  - Ứng dụng web: Frontend React + Backend Spring Boot
  - Chạy trên môi trường Docker Compose trong phát triển

### 1.3. Mục tiêu chính

- Cho phép người dùng đăng ký, đăng nhập và quản lý tài khoản cá nhân.
- Hỗ trợ các giao dịch ví cơ bản: chuyển tiền, nạp tiền, rút tiền.
- Cung cấp lịch sử giao dịch và thông báo cho người dùng.
- Cung cấp dashboard cho admin để quản lý người dùng, giao dịch và log hệ thống.

---

## 2. Kiến trúc Hệ thống & Tech Stack

### 2.1. Sơ đồ kiến trúc phân tầng (Layered Architecture)

```text
[ Frontend: React + TypeScript + Vite ]
        │
        │ HTTP / JSON / Auth Token
        ▼
[ Security Layer: Spring Security + JWT ]
        │
        ▼
[ Controller Layer: REST Controllers ]
        │
        ▼
[ Service Layer: Business Logic ]
        │
        ▼
[ Repository Layer: Spring Data JPA ]
        │
        ▼
[ Database & External Services: MySQL | RabbitMQ | Docker ]
```

### 2.2. Chi tiết bộ công nghệ (Tech Stack)

- Backend Framework: Java 17+, Spring Boot 3.x
- Frontend: React, TypeScript, Vite, Tailwind CSS
- Security: Spring Security, JWT
- Database: MySQL
- Messaging/Async: RabbitMQ
- Containerization: Docker Compose
- Build Tool: Maven

---

## 3. Đặc tả Yêu cầu Chức năng (Functional Requirements)

### 3.1. Phân hệ Xác thực & Phân quyền (Authentication & Authorization)

- FR-01 (Đăng ký tài khoản):
  Hệ thống cho phép người dùng đăng ký tài khoản mới bằng thông tin cơ bản như tên, số điện thoại/email và mật khẩu.

- FR-02 (Đăng nhập & Phân quyền):
  Hệ thống cho phép người dùng đăng nhập bằng thông tin hợp lệ và phân quyền theo vai trò User hoặc Admin.

- FR-03 (Xác thực OTP):
  Sau khi đăng ký, hệ thống phải gửi OTP xác thực để kích hoạt tài khoản.

- FR-04 (Quên mật khẩu & Đặt lại mật khẩu):
  Hệ thống cho phép người dùng yêu cầu reset mật khẩu và đặt lại mật khẩu mới sau khi xác thực thành công.

### 3.2. Phân hệ Quản lý Người dùng & Hồ sơ (User Profile Management)

- FR-05 (Quản lý hồ sơ người dùng):
  Người dùng có thể xem và cập nhật thông tin cá nhân, đổi mật khẩu và tải avatar.

- FR-06 (Tra cứu người nhận):
  Hệ thống cho phép người dùng tra cứu người nhận theo số điện thoại để thực hiện chuyển tiền.

### 3.3. Phân hệ Ví điện tử & Giao dịch (Wallet & Transaction Management)

- FR-07 (Xem thông tin ví):
  Người dùng có thể xem số dư hiện tại và thông tin ví của mình.

- FR-08 (Chuyển tiền):
  Người dùng có thể chuyển tiền cho người dùng khác trong hệ thống bằng cách nhập số điện thoại người nhận, số tiền và lời nhắn. Giao dịch cần được xác thực bằng OTP trước khi hoàn tất.

- FR-09 (Nạp tiền):
  Hệ thống cho phép người dùng tạo giao dịch nạp tiền vào ví.

- FR-10 (Rút tiền):
  Hệ thống cho phép người dùng tạo giao dịch rút tiền khỏi ví.

- FR-11 (Xem lịch sử giao dịch):
  Người dùng có thể xem lịch sử giao dịch của mình, lọc theo loại giao dịch và thời gian, đồng thời xem chi tiết từng giao dịch.

### 3.4. Phân hệ Thông báo (Notification System)

- FR-12 (Xem thông báo):
  Người dùng có thể xem danh sách thông báo liên quan đến trạng thái giao dịch hoặc hệ thống.

- FR-13 (Đánh dấu thông báo đã đọc):
  Người dùng có thể đánh dấu một thông báo là đã đọc.

### 3.5. Phân hệ Quản trị (Admin Management)

- FR-14 (Dashboard Admin):
  Admin có thể xem dashboard tổng quan về hệ thống.

- FR-15 (Quản lý người dùng):
  Admin có thể xem danh sách người dùng, khóa hoặc mở khóa tài khoản.

- FR-16 (Quản lý giao dịch):
  Admin có thể xem giao dịch, duyệt hoặc từ chối giao dịch đáng ngờ.

- FR-17 (Quản lý log & cảnh báo):
  Admin có thể xem audit log và các sự kiện hệ thống.

---

## 4. Thiết kế Cơ sở Dữ liệu (Database Schema Design)

### 4.1. Sơ đồ quan hệ bảng (Entity Relationship)

```text
user 1 --- 1 wallet
user 1 --- N transactions
user 1 --- N notifications
user 1 --- N refresh_tokens
user 1 --- N otps
```

### 4.2. Cấu trúc chi tiết các bảng

| Bảng | Mô tả | Thành phần chính |
|------|------|------------------|
| users | Lưu thông tin tài khoản đăng nhập và vai trò | id, name, phone, email, password, role |
| wallets | Lưu thông tin ví của người dùng | id, user_id, balance, status |
| transactions | Lưu lịch sử giao dịch | id, user_id, type, amount, status, message, transaction_code |
| refresh_tokens | Lưu token làm mới phiên đăng nhập | id, user_id, token, expiry |
| otps | Lưu mã OTP cho đăng ký, reset mật khẩu và xác nhận giao dịch | id, identifier, otp_code, expiry |
| notifications | Lưu thông báo cho người dùng | id, user_id, title, content, is_read |
| audit_logs | Lưu lịch sử thao tác quản trị | id, action, actor, target, created_at |

### 4.3. Mô tả bảng chính

| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc / Mô tả |
|--------|--------------|------|-------------------|
| users.id | BIGINT | PK | Khóa chính, tự tăng |
| users.name | VARCHAR(150) |  | Tên người dùng |
| users.phone | VARCHAR(20) | UNIQUE | Số điện thoại đăng nhập |
| users.email | VARCHAR(150) |  | Email người dùng |
| users.password | VARCHAR(255) |  | Mật khẩu đã mã hóa |
| users.role | VARCHAR(20) |  | Vai trò: USER, ADMIN |
| wallets.id | BIGINT | PK | Khóa chính |
| wallets.user_id | BIGINT | FK | Tham chiếu users.id |
| wallets.balance | DECIMAL(12,2) |  | Số dư hiện tại |
| wallets.status | VARCHAR(20) |  | Trạng thái ví: ACTIVE, LOCKED, PENDING |
| transactions.id | BIGINT | PK | Khóa chính |
| transactions.user_id | BIGINT | FK | Người thực hiện giao dịch |
| transactions.type | VARCHAR(20) |  | TRANSFER, DEPOSIT, WITHDRAW |
| transactions.amount | DECIMAL(12,2) |  | Số tiền |
| transactions.status | VARCHAR(20) |  | SUCCESS, PENDING, FAILED |
| transactions.message | TEXT |  | Nội dung giao dịch |
| notifications.is_read | BOOLEAN |  | Trạng thái đã đọc |

---

## 5. Danh sách RESTful APIs Chính

| Method | Endpoint | Vai trò | Mô tả | Tham số |
|--------|----------|---------|--------|---------|
| POST | /api/v1/auth/register | Public | Đăng ký tài khoản | body: user info |
| POST | /api/v1/auth/login | Public | Đăng nhập | body: phone/email + password |
| POST | /api/v1/auth/verify-otp | Public | Xác thực OTP | body: identifier + otp |
| POST | /api/v1/auth/refresh | Public | Làm mới token | header/token |
| GET | /api/v1/auth/me | User/Admin | Lấy thông tin user hiện tại | token |
| POST | /api/v1/auth/logout | User/Admin | Đăng xuất | token |
| POST | /api/v1/auth/forgot-password | Public | Gửi OTP reset mật khẩu | body: email/phone |
| POST | /api/v1/auth/reset-password | Public | Đặt lại mật khẩu | body: new password |
| GET | /api/v1/users/phone/{phone} | User/Admin | Tra cứu người nhận | path param |
| PUT | /api/v1/users/profile | User/Admin | Cập nhật hồ sơ | body: profile info |
| PUT | /api/v1/users/change-password | User/Admin | Đổi mật khẩu | body: current + new password |
| POST | /api/v1/users/avatar | User/Admin | Upload avatar | form-data file |
| POST | /api/v1/wallets/transfer/initiate | User/Admin | Khởi tạo giao dịch chuyển tiền | body: receiverPhone + amount |
| POST | /api/v1/wallets/transfer | User/Admin | Xác nhận chuyển tiền | body: transfer request + otp |
| GET | /api/v1/wallets/me | User/Admin | Lấy thông tin ví | token |
| GET | /api/v1/wallets/history | User/Admin | Lấy lịch sử giao dịch | query: page, size |
| POST | /api/v1/wallets/deposit | User/Admin | Nạp tiền | body: amount |
| POST | /api/v1/wallets/withdraw | User/Admin | Rút tiền | body: amount |
| GET | /api/v1/transactions/me | User/Admin | Lấy giao dịch cá nhân | query: filters |
| GET | /api/v1/notifications | User/Admin | Lấy thông báo | token |
| PATCH | /api/v1/notifications/{id}/read | User/Admin | Đánh dấu đã đọc | path param |
| GET | /api/v1/admin/dashboard | Admin | Lấy dashboard admin | token |
| GET | /api/v1/admin/transactions | Admin | Xem danh sách giao dịch | query: page, size |
| POST | /api/v1/admin/transactions/{id}/approve | Admin | Duyệt giao dịch | path param |
| POST | /api/v1/admin/transactions/{id}/reject | Admin | Từ chối giao dịch | path param |
| GET | /api/v1/admin/logs | Admin | Xem log hệ thống | token |

---

## 6. Yêu cầu Phi Chức năng & Trải nghiệm Người dùng (NFR & UX)

### 6.1. Hiệu năng (Performance)
- Các thao tác chính như đăng nhập, xem số dư và lịch sử giao dịch phải phản hồi nhanh.
- Đối với môi trường phát triển/kiểm thử, thời gian phản hồi mục tiêu là dưới 3 giây cho các request thông thường.

### 6.2. Độ tin cậy & Tự động hóa (Reliability)
- Giao dịch chuyển tiền phải được xử lý nhất quán và an toàn.
- Hệ thống phải đảm bảo số dư và lịch sử giao dịch được cập nhật đúng sau mỗi giao dịch thành công.
- Các tác vụ thông báo và quản trị phải có khả năng vận hành ổn định khi có lỗi.

### 6.3. Bảo mật (Security)
- Hệ thống phải sử dụng JWT để xác thực người dùng.
- Phải phân quyền rõ ràng giữa User và Admin.
- Mật khẩu phải được lưu theo cơ chế mã hóa phù hợp.
- Các endpoint nhạy cảm phải kiểm tra quyền truy cập trước khi thực hiện.

### 6.4. Khả năng bảo trì (Maintainability)
- Mã nguồn phải chia tầng rõ ràng: Controller, Service, Repository, Entity, DTO.
- Cấu trúc module phải dễ mở rộng cho các chức năng mới.

### 6.5. Trực quan hóa trạng thái (UI/UX Indicators)
- Giao diện cần hiển thị rõ trạng thái đăng nhập, trạng thái giao dịch, trạng thái ví và thông báo hệ thống.
- Người dùng nên nhận được phản hồi trực quan sau mỗi thao tác quan trọng như chuyển tiền, nạp/rút tiền và cập nhật hồ sơ.

---

## 7. Luồng nghiệp vụ chính

### 7.1. Luồng đăng ký và kích hoạt tài khoản
1. User nhập thông tin đăng ký.
2. Hệ thống tạo tài khoản mới.
3. Hệ thống gửi OTP xác thực.
4. User nhập OTP.
5. Hệ thống kích hoạt tài khoản và tạo ví điện tử.

### 7.2. Luồng đăng nhập
1. User nhập thông tin đăng nhập.
2. Hệ thống xác thực thông tin.
3. Nếu hợp lệ, hệ thống cấp token và chuyển hướng tới màn hình phù hợp.

### 7.3. Luồng chuyển tiền
1. User chọn chức năng gửi tiền.
2. Hệ thống yêu cầu nhập người nhận, số tiền và lời nhắn.
3. Hệ thống tạo giao dịch và yêu cầu OTP xác nhận.
4. User nhập OTP.
5. Hệ thống thực hiện giao dịch và cập nhật số dư.

### 7.4. Luồng xem lịch sử và số dư
1. User truy cập trang ví hoặc giao dịch.
2. Hệ thống gọi API lấy thông tin ví và lịch sử giao dịch.
3. Hệ thống hiển thị dữ liệu cho người dùng.

### 7.5. Luồng quản trị
1. Admin đăng nhập vào hệ thống.
2. Admin truy cập dashboard.
3. Admin xem thống kê, quản lý người dùng và giao dịch.
4. Admin duyệt/từ chối giao dịch hoặc khóa/mở khóa tài khoản.

---

## 8. Ràng buộc và giả định

- Hệ thống giả định người dùng có số điện thoại hoặc email dùng để đăng ký.
- Hệ thống hiện tại chưa tích hợp cổng thanh toán ngân hàng thật.
- OTP được xử lý trong hệ thống nội bộ để hỗ trợ demo và phát triển.
- Môi trường chạy chính là Docker Compose cho phát triển và kiểm thử.

---

## 9. Tiêu chí chấp nhận

Hệ thống được coi là hoàn thành cơ bản khi các điều kiện sau được thỏa mãn:
- Người dùng có thể đăng ký, đăng nhập và xác thực tài khoản thành công.
- Người dùng có thể chuyển tiền, nạp tiền, rút tiền và xem lịch sử giao dịch.
- Người dùng có thể cập nhật hồ sơ và xem thông tin ví.
- Admin có thể xem dashboard và quản lý người dùng, giao dịch và log hệ thống.
- Hệ thống bảo vệ các endpoint theo quyền truy cập phù hợp.

---

## 10. Kết luận

Tài liệu này mô tả các yêu cầu chức năng và phi chức năng cơ bản của hệ thống ví điện tử. Đây là bản đặc tả ban đầu có thể tiếp tục cập nhật khi hệ thống phát triển thêm các tính năng mới như tích hợp cổng thanh toán thực tế, phân tích giao dịch nâng cao, báo cáo doanh thu và cảnh báo tự động.
