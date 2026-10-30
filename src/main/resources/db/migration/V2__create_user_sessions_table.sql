CREATE TABLE user_sessions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users (id),
    phone_number VARCHAR(50) NOT NULL,
    token         VARCHAR(2000) NOT NULL,
    is_active     BOOLEAN NOT NULL,
    created_at    TIMESTAMP,
    ended_at      TIMESTAMP,
    end_reason    VARCHAR(50)
);

CREATE UNIQUE INDEX uk_user_sessions_token ON user_sessions (token);
CREATE INDEX ix_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX ix_user_sessions_phone_number ON user_sessions (phone_number);