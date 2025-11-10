-- ═══════════════════════════════════════════════════════════════════
-- V9: Add user_ipn Field
-- Description: Add IPN (Identifiant Personnel National) field to users
-- ═══════════════════════════════════════════════════════════════════

-- Add user_ipn column
ALTER TABLE users ADD COLUMN user_ipn VARCHAR(50) UNIQUE;

-- Add index for performance
CREATE INDEX idx_users_ipn ON users(user_ipn) WHERE user_ipn IS NOT NULL;

-- Add comment
COMMENT ON COLUMN users.user_ipn IS 'Identifiant Personnel National (e.g., d179090)';

-- Update existing users with sample IPNs
UPDATE users SET user_ipn = 'd179001' WHERE employee_number = 'EMP001';
UPDATE users SET user_ipn = 'd179002' WHERE employee_number = 'EMP002';
UPDATE users SET user_ipn = 'd179003' WHERE employee_number = 'EMP003';
UPDATE users SET user_ipn = 'd179004' WHERE employee_number = 'EMP004';
UPDATE users SET user_ipn = 'd179005' WHERE employee_number = 'EMP005';
