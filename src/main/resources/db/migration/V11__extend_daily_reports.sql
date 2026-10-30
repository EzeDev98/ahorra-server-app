ALTER TABLE daily_reports
    ADD COLUMN period_type             VARCHAR(20),
    ADD COLUMN period_start            DATE,
    ADD COLUMN period_end              DATE,
    ADD COLUMN entry_count             INTEGER,
    ADD COLUMN day_spend               NUMERIC(15, 2),
    ADD COLUMN baseline                NUMERIC(15, 2),
    ADD COLUMN income_max              NUMERIC(15, 2),
    ADD COLUMN period_spend            NUMERIC(15, 2),
    ADD COLUMN variance                NUMERIC(15, 2),
    ADD COLUMN rolling_avg_daily_spend NUMERIC(15, 2),
    ADD COLUMN days_observed           INTEGER,
    ADD COLUMN verdict                 VARCHAR(20),
    ADD COLUMN insight_text            VARCHAR(500),
    ADD COLUMN narrative_source        VARCHAR(20);

ALTER TABLE daily_reports
    ADD CONSTRAINT ck_daily_reports_verdict
        CHECK (verdict IS NULL OR verdict IN ('OVER', 'ON_TRACK', 'UNDER', 'NO_INCOME_SET')),
    ADD CONSTRAINT ck_daily_reports_narrative_source
        CHECK (narrative_source IS NULL OR narrative_source IN ('AI', 'TEMPLATE'));

CREATE TABLE daily_report_categories (
    id                BIGSERIAL PRIMARY KEY,
    daily_report_id   BIGINT          NOT NULL REFERENCES daily_reports (id),
    scope             VARCHAR(10)     NOT NULL,
    position          INTEGER         NOT NULL,
    category_name     VARCHAR(255)    NOT NULL,
    amount            NUMERIC(15, 2) NOT NULL,
    share_percent     NUMERIC(6, 2) NOT NULL,
    attributed_excess NUMERIC(15, 2),
    CONSTRAINT ck_daily_report_categories_scope CHECK (scope IN ('DAY', 'PERIOD'))
);

CREATE INDEX ix_daily_report_categories_report ON daily_report_categories (daily_report_id);