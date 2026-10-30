CREATE TABLE users (
    id                 BIGSERIAL PRIMARY KEY,
    internal_auth_id   VARCHAR(255) NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    middle_name        VARCHAR(255),
    last_name          VARCHAR(255) NOT NULL,
    username           VARCHAR(255) NOT NULL,
    email              VARCHAR(255) NOT NULL,
    phone_number       VARCHAR(50) NOT NULL,
    age                INTEGER       NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    date_of_birth      VARCHAR(20) NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_users_internal_auth_id ON users (internal_auth_id);
CREATE UNIQUE INDEX uk_users_username ON users (username);
CREATE UNIQUE INDEX uk_users_email ON users (email);
CREATE UNIQUE INDEX uk_users_phone_number ON users (phone_number);