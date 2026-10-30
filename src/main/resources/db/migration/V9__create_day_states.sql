CREATE TABLE day_states (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users (id),
    day_date      DATE        NOT NULL,
    status        VARCHAR(20) NOT NULL,
    suspended_at TIMESTAMP,
    closed_at     TIMESTAMP,
    CONSTRAINT uk_day_states_user_day UNIQUE (user_id, day_date),
    CONSTRAINT ck_day_states_status CHECK (status IN ('SUSPENDED', 'CLOSED')),
    CONSTRAINT ck_day_states_lifecycle CHECK (
        (status = 'SUSPENDED' AND suspended_at IS NOT NULL AND closed_at IS NULL)
        OR (status = 'CLOSED' AND closed_at IS NOT NULL)
    )
);