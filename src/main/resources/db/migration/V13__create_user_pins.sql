CREATE TABLE user_pins (
    user_id           BIGINT       NOT NULL PRIMARY KEY,
    pin_hash          VARCHAR(100) NOT NULL,
    failed_attempts   INTEGER      NOT NULL DEFAULT 0,
    locked_until      TIMESTAMP,
    login_verified_at TIMESTAMP,
    changed_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_pins_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);