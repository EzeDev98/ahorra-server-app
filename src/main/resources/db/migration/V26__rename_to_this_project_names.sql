-- This project names three things differently from V1 to V25: the user's status column, the email
-- verification table and the excess column on report categories. Rename them to the names the entities
-- use, so Hibernate's validate passes. The data and every constraint stay as they are.
ALTER TABLE users RENAME COLUMN status TO user_status;
ALTER TABLE email_verifications RENAME TO email_verification;
ALTER TABLE daily_report_categories RENAME COLUMN attributed_excess TO attribute_excess;