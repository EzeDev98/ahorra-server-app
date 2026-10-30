CREATE TABLE password_resets (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    otp_hash          VARCHAR(255) NOT NULL,
    otp_expires_at    TIMESTAMP    NOT NULL,
    attempts          INT          NOT NULL DEFAULT 0,
    reset_token_hash VARCHAR(64),
    token_expires_at TIMESTAMP,
    used_at           TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_resets_user ON password_resets (user_id);
CREATE UNIQUE INDEX uk_password_resets_token ON password_resets (reset_token_hash) WHERE reset_token_hash IS NOT NULL;