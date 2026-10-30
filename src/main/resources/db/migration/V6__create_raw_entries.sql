CREATE TABLE raw_entries (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users (id),
    source          VARCHAR(10)   NOT NULL,
    raw_text        VARCHAR(2000) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'RECEIVED',
    failure_reason VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_raw_entries_source CHECK (source IN ('TEXT', 'VOICE')),
    CONSTRAINT ck_raw_entries_status CHECK (status IN ('RECEIVED', 'PARSED', 'FAILED'))
);

CREATE INDEX ix_raw_entries_user_created ON raw_entries (user_id, created_at);