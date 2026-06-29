CREATE TABLE IF NOT EXISTS otps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    otp_code VARCHAR(6) NOT NULL,
    user_id BIGINT NOT NULL,
    expired_at DATETIME NOT NULL,
    verified TINYINT(1) NOT NULL DEFAULT 0,
    amount DECIMAL(19, 2),
    receiver_phone VARCHAR(20),
    failed_attempts INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expired_at (expired_at),
    INDEX idx_verified (verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
