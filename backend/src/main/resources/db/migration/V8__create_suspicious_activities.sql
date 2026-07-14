CREATE TABLE suspicious_activities (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    reason VARCHAR(255) NOT NULL,

    details VARCHAR(500),

    detected_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_suspicious_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);