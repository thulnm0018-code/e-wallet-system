CREATE TABLE linked_bank_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    bank_name VARCHAR(100) NOT NULL,

    account_number VARCHAR(50) NOT NULL,

    account_holder_name VARCHAR(255) NOT NULL,

    linked_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_bank_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_user_bank_account
        UNIQUE (user_id, account_number)
);