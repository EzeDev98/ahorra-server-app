ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET email_verified = phone_verified;

ALTER TABLE users DROP COLUMN phone_verified;

ALTER TABLE users DROP COLUMN phone_number;

ALTER TABLE phone_verifications RENAME TO email_verifications;
ALTER INDEX idx_phone_verifications_user RENAME TO idx_email_verifications_user;

ALTER TABLE user_sessions DROP COLUMN phone_number;