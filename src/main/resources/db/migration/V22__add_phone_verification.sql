ALTER TABLE users ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET phone_verified = TRUE;

CREATE TABLE phone_verifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    otp_hash        VARCHAR(255) NOT NULL,
    otp_expires_at TIMESTAMP     NOT NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    used_at         TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_phone_verifications_user ON phone_verifications (user_id);