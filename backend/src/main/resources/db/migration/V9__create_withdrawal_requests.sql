CREATE TABLE withdrawal_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    amount DECIMAL(15,2) NOT NULL,

    reserved_amount DECIMAL(15,2) NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL,

    idempotency_key VARCHAR(100),

    created_at TIMESTAMP NOT NULL,

    approved_at TIMESTAMP NULL,

    rejected_at TIMESTAMP NULL,

    CONSTRAINT fk_withdraw_request_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    UNIQUE KEY uq_withdrawal_idempotency_user (
        idempotency_key,
        user_id
    )
);

CREATE INDEX idx_withdraw_request_user
    ON withdrawal_requests(user_id);

CREATE INDEX idx_withdraw_request_status
    ON withdrawal_requests(status);

CREATE INDEX idx_withdraw_request_created_at
    ON withdrawal_requests(created_at);