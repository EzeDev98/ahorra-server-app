CREATE TABLE accounts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_accounts_type CHECK (type IN ('ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE'))
);

CREATE UNIQUE INDEX uk_accounts_user_name ON accounts (user_id, name);
CREATE INDEX ix_accounts_user_id ON accounts (user_id);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    name        VARCHAR(255) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_categories_user_name ON categories (user_id, name);
CREATE INDEX ix_categories_user_id ON categories (user_id);