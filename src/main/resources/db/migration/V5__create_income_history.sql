CREATE TABLE income_history (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT         NOT NULL REFERENCES users (id),
    min_amount      NUMERIC(15, 2) NOT NULL,
    max_amount      NUMERIC(15, 2) NOT NULL,
    period_type     VARCHAR(20)    NOT NULL,
    period_length   INTEGER,
    effective_from DATE            NOT NULL,
    effective_to    DATE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_income_history_period_type CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'CUSTOM')),
    CONSTRAINT ck_income_history_range CHECK (min_amount >= 0 AND max_amount >= min_amount),
    CONSTRAINT ck_income_history_period_length CHECK (
        (period_type = 'CUSTOM' AND period_length IS NOT NULL AND period_length > 0)
        OR (period_type <> 'CUSTOM' AND period_length IS NULL)
    ),
    CONSTRAINT ck_income_history_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ix_income_history_user_from ON income_history (user_id, effective_from);

CREATE UNIQUE INDEX uk_income_history_one_current
    ON income_history (user_id) WHERE effective_to IS NULL;