CREATE TABLE obligations (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT         NOT NULL REFERENCES users (id),
    name        VARCHAR(60)    NOT NULL,
    amount      NUMERIC(15, 2) NOT NULL,
    cadence     VARCHAR(20)    NOT NULL,
    is_active   BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_obligations_cadence CHECK (cadence IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT ck_obligations_amount CHECK (amount > 0)
);

CREATE INDEX ix_obligations_user_id ON obligations (user_id);