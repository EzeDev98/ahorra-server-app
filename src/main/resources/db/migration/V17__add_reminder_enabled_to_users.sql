ALTER TABLE users ADD COLUMN reminder_enabled BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE users SET reminder_enabled = TRUE WHERE id IN (SELECT DISTINCT user_id FROM push_subscriptions);