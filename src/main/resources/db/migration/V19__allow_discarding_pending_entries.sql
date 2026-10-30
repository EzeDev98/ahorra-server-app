ALTER TABLE pending_entries ADD COLUMN discarded_at TIMESTAMP;

ALTER TABLE pending_entries DROP CONSTRAINT ck_pending_entries_status;
ALTER TABLE pending_entries
    ADD CONSTRAINT ck_pending_entries_status CHECK (status IN ('PENDING', 'CONFIRMED', 'EDITED', 'DISCARDED'));

ALTER TABLE pending_entries DROP CONSTRAINT ck_pending_entries_lifecycle;
ALTER TABLE pending_entries
    ADD CONSTRAINT ck_pending_entries_lifecycle CHECK (
        (status = 'PENDING' AND ledger_entry_id IS NULL AND confirmed_at IS NULL AND discarded_at IS NULL)
         OR (status IN ('CONFIRMED', 'EDITED') AND ledger_entry_id IS NOT NULL AND confirmed_at IS NOT NULL AND discarded_at IS NULL)
         OR (status = 'DISCARDED' AND ledger_entry_id IS NULL AND confirmed_at IS NULL AND discarded_at IS NOT NULL)
    );