CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_code VARCHAR(100) NOT NULL UNIQUE,
    idempotency_key VARCHAR(100) UNIQUE,

    sender_wallet_id BIGINT,
    receiver_wallet_id BIGINT,

    amount DECIMAL(15, 2) NOT NULL,
    service_fee DECIMAL(15, 2) NOT NULL DEFAULT 0,

    message VARCHAR(255),

    status VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,

    payment_method VARCHAR(100),
    approved_by BIGINT,
    approved_at DATETIME,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    CONSTRAINT fk_transaction_sender
        FOREIGN KEY (sender_wallet_id)
        REFERENCES wallets(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_transaction_receiver
        FOREIGN KEY (receiver_wallet_id)
        REFERENCES wallets(id)
        ON DELETE SET NULL,

    INDEX idx_transaction_code (transaction_code),
    INDEX idx_sender_wallet (sender_wallet_id),
    INDEX idx_receiver_wallet (receiver_wallet_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;