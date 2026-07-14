CREATE TABLE audit_logs (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NULL,

    action VARCHAR(50) NOT NULL,

    description VARCHAR(1000),

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_audit_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_audit_user
ON audit_logs(user_id);

CREATE INDEX idx_audit_action
ON audit_logs(action);

CREATE INDEX idx_audit_created
ON audit_logs(created_at);