ALTER TABLE pending_entries
    ADD COLUMN ledger_entry_id BIGINT REFERENCES ledger_entries (id),
    ADD COLUMN confirmed_at    TIMESTAMP;

CREATE UNIQUE INDEX uk_pending_entries_ledger_entry
    ON pending_entries (ledger_entry_id) WHERE ledger_entry_id IS NOT NULL;

ALTER TABLE pending_entries
    ADD CONSTRAINT ck_pending_entries_lifecycle CHECK (
        (status = 'PENDING' AND ledger_entry_id IS NULL AND confirmed_at IS NULL)
        OR (status IN ('CONFIRMED', 'EDITED') AND ledger_entry_id IS NOT NULL AND confirmed_at IS NOT NULL)
    );