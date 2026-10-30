CREATE TABLE ledger_entries (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id),
    category_id BIGINT        REFERENCES categories (id),
    entry_date   DATE         NOT NULL,
    description VARCHAR(500),
    posted_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX ix_ledger_entries_user_date ON ledger_entries (user_id, entry_date);
CREATE INDEX ix_ledger_entries_category_id ON ledger_entries (category_id);

CREATE TABLE transaction_entries (
    id               BIGSERIAL PRIMARY KEY,
    ledger_entry_id BIGINT          NOT NULL REFERENCES ledger_entries (id),
    account_id       BIGINT         NOT NULL REFERENCES accounts (id),
    direction        VARCHAR(10)    NOT NULL,
    amount           NUMERIC(15, 2) NOT NULL,
    CONSTRAINT ck_transaction_entries_direction CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT ck_transaction_entries_amount_positive CHECK (amount > 0)
);

CREATE INDEX ix_transaction_entries_ledger_entry_id ON transaction_entries (ledger_entry_id);
CREATE INDEX ix_transaction_entries_account_id ON transaction_entries (account_id);

CREATE OR REPLACE FUNCTION check_ledger_entry_balanced() RETURNS TRIGGER AS $$
DECLARE
    target_id     BIGINT;
    debit_total NUMERIC(15, 2);
    credit_total NUMERIC(15, 2);
BEGIN
    IF TG_OP = 'DELETE' THEN
         target_id := OLD.ledger_entry_id;
    ELSE
         target_id := NEW.ledger_entry_id;
    END IF;

    SELECT COALESCE(SUM(amount) FILTER (WHERE direction = 'DEBIT'), 0),
           COALESCE(SUM(amount) FILTER (WHERE direction = 'CREDIT'), 0)
      INTO debit_total, credit_total
      FROM transaction_entries
     WHERE ledger_entry_id = target_id;

    IF debit_total <> credit_total THEN
        RAISE EXCEPTION 'Ledger entry % is unbalanced: debits % <> credits %',
            target_id, debit_total, credit_total;
    END IF;

     RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_transaction_entries_balanced
    AFTER INSERT OR UPDATE OR DELETE ON transaction_entries
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION check_ledger_entry_balanced();