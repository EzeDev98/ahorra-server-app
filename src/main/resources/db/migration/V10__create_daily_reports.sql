CREATE TABLE daily_reports (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT        NOT NULL REFERENCES users (id),
    report_date   DATE          NOT NULL,
    kind          VARCHAR(20)   NOT NULL,
    summary_text VARCHAR(1000) NOT NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_reports_user_date UNIQUE (user_id, report_date),
    CONSTRAINT ck_daily_reports_kind CHECK (kind IN ('NO_ENTRIES', 'STANDARD'))
);