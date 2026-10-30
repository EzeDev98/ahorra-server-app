ALTER TABLE raw_entries ADD COLUMN client_request_id VARCHAR(100);

CREATE UNIQUE INDEX uk_raw_entries_user_client_request
    ON raw_entries (user_id, client_request_id) WHERE client_request_id IS NOT NULL;

CREATE TABLE pending_entries (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT         NOT NULL REFERENCES users (id),
    raw_entry_id BIGINT          NOT NULL REFERENCES raw_entries (id),
    flow          VARCHAR(10)    NOT NULL,
    entry_date    DATE           NOT NULL,
    vendor        VARCHAR(255),
    category_id   BIGINT         REFERENCES categories (id),
    account_id    BIGINT         REFERENCES accounts (id),
    total_amount NUMERIC(15, 2) NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pending_entries_raw_entry UNIQUE (raw_entry_id),
    CONSTRAINT ck_pending_entries_flow CHECK (flow IN ('EXPENSE', 'INCOME')),
    CONSTRAINT ck_pending_entries_status CHECK (status IN ('PENDING', 'CONFIRMED', 'EDITED')),
    CONSTRAINT ck_pending_entries_total CHECK (total_amount > 0)
);

CREATE INDEX ix_pending_entries_user_status_date ON pending_entries (user_id, status, entry_date);

CREATE TABLE pending_entry_items (
    id                BIGSERIAL PRIMARY KEY,
    pending_entry_id BIGINT          NOT NULL REFERENCES pending_entries (id),
    position          INTEGER        NOT NULL,
    description       VARCHAR(255)   NOT NULL,
    amount            NUMERIC(15, 2) NOT NULL,
    CONSTRAINT ck_pending_entry_items_amount CHECK (amount > 0)
);

CREATE INDEX ix_pending_entry_items_pending_entry_id ON pending_entry_items (pending_entry_id);