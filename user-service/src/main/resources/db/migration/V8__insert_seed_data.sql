-- ═══════════════════════════════════════════════════════════════════
-- V8: Insert Seed Data
-- Description: Initial test data for development and testing
-- ═══════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────
-- Sample Business Units
-- ─────────────────────────────────────────────────────────────────
INSERT INTO business_units (code, name, legal_name, type, region_code, region_name, rrf_code,
    phone_number, email, address_line1, postal_code, city, country, is_active, opening_date)
VALUES
    ('BU001', 'Paris Concession', 'Eflo Paris SARL', 'DEALERSHIP', 'IDF', 'Île-de-France', 'RRF75',
     '+33 1 23 45 67 89', 'paris@eflo.com', '123 Avenue des Champs-Élysées', '75008', 'Paris', 'FR', true, '2020-01-01'),

    ('BU002', 'Lyon Service Center', 'Eflo Lyon Service', 'SERVICE_CENTER', 'ARA', 'Auvergne-Rhône-Alpes', 'RRF69',
     '+33 4 78 90 12 34', 'lyon@eflo.com', '45 Rue de la République', '69002', 'Lyon', 'FR', true, '2020-06-01'),

    ('BU003', 'Marseille Dealership', 'Eflo Marseille SA', 'DEALERSHIP', 'PAC', 'Provence-Alpes-Côte d''Azur', 'RRF13',
     '+33 4 91 23 45 67', 'marseille@eflo.com', '78 La Canebière', '13001', 'Marseille', 'FR', true, '2021-01-01'),

    ('BU004', 'Headquarters', 'Eflo France Headquarters', 'HEADQUARTERS', 'IDF', 'Île-de-France', 'RRFHQ',
     '+33 1 99 88 77 66', 'hq@eflo.com', '1 Place de la Défense', '92400', 'Paris', 'FR', true, '2019-01-01');

-- ─────────────────────────────────────────────────────────────────
-- Sample Users (synced from Keycloak)
-- Note: These UUIDs should match actual Keycloak users
-- ─────────────────────────────────────────────────────────────────
INSERT INTO users (keycloak_id, keycloak_username, employee_number, email, first_name, last_name,
    phone_number, mobile_number, job_title, department, is_active, hire_date, login_count)
VALUES
    -- Admin user
    ('11111111-1111-1111-1111-111111111111', 'admin', 'EMP001', 'admin@eflo.com', 'Admin', 'Eflo',
     '+33 1 11 11 11 11', '+33 6 11 11 11 11', 'System Administrator', 'IT', true, '2019-01-01', 0),

    -- Sales Manager
    ('22222222-2222-2222-2222-222222222222', 'jane.smith', 'EMP002', 'jane.smith@eflo.com', 'Jane', 'Smith',
     '+33 1 22 22 22 22', '+33 6 22 22 22 22', 'Sales Manager', 'Sales', true, '2020-01-15', 0),

    -- Salesperson
    ('33333333-3333-3333-3333-333333333333', 'john.doe', 'EMP003', 'john.doe@eflo.com', 'John', 'Doe',
     '+33 1 33 33 33 33', '+33 6 33 33 33 33', 'Sales Representative', 'Sales', true, '2020-03-01', 0),

    -- Accountant
    ('44444444-4444-4444-4444-444444444444', 'bob.johnson', 'EMP004', 'bob.johnson@eflo.com', 'Bob', 'Johnson',
     '+33 1 44 44 44 44', '+33 6 44 44 44 44', 'Accountant', 'Finance', true, '2020-02-01', 0),

    -- Secretary
    ('55555555-5555-5555-5555-555555555555', 'alice.martin', 'EMP005', 'alice.martin@eflo.com', 'Alice', 'Martin',
     '+33 1 55 55 55 55', '+33 6 55 55 55 55', 'Executive Secretary', 'Administration', true, '2021-01-10', 0);

-- ─────────────────────────────────────────────────────────────────
-- Update Business Units with Managers
-- ─────────────────────────────────────────────────────────────────
UPDATE business_units SET manager_id = (SELECT id FROM users WHERE employee_number = 'EMP002'),
    manager_name = 'Jane Smith' WHERE code = 'BU001';

UPDATE business_units SET manager_id = (SELECT id FROM users WHERE employee_number = 'EMP002'),
    manager_name = 'Jane Smith' WHERE code = 'BU002';

UPDATE business_units SET manager_id = (SELECT id FROM users WHERE employee_number = 'EMP002'),
    manager_name = 'Jane Smith' WHERE code = 'BU003';

UPDATE business_units SET manager_id = (SELECT id FROM users WHERE employee_number = 'EMP001'),
    manager_name = 'Admin Eflo' WHERE code = 'BU004';

-- ─────────────────────────────────────────────────────────────────
-- User-Business Unit Assignments
-- ─────────────────────────────────────────────────────────────────
INSERT INTO user_business_units (user_id, business_unit_id, is_primary, is_active, assigned_at, assigned_by)
VALUES
    -- Admin at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP001'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     true, true, '2019-01-01', 'system'),

    -- Jane at Paris (primary) and Lyon
    ((SELECT id FROM users WHERE employee_number = 'EMP002'),
     (SELECT id FROM business_units WHERE code = 'BU001'),
     true, true, '2020-01-15', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP002'),
     (SELECT id FROM business_units WHERE code = 'BU002'),
     false, true, '2021-01-01', 'system'),

    -- John at Paris
    ((SELECT id FROM users WHERE employee_number = 'EMP003'),
     (SELECT id FROM business_units WHERE code = 'BU001'),
     true, true, '2020-03-01', 'system'),

    -- Bob at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP004'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     true, true, '2020-02-01', 'system'),

    -- Alice at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP005'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     true, true, '2021-01-10', 'system');

-- ─────────────────────────────────────────────────────────────────
-- User Roles (synced from Keycloak)
-- ─────────────────────────────────────────────────────────────────
INSERT INTO user_roles (user_id, role_name, source, is_active, assigned_at, assigned_by)
VALUES
    ((SELECT id FROM users WHERE employee_number = 'EMP001'), 'SUPER_ADMIN', 'KEYCLOAK', true, '2019-01-01', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP002'), 'SALES_MANAGER', 'KEYCLOAK', true, '2020-01-15', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP003'), 'SALESPERSON', 'KEYCLOAK', true, '2020-03-01', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP004'), 'ACCOUNTANT', 'KEYCLOAK', true, '2020-02-01', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP005'), 'SECRETARY', 'KEYCLOAK', true, '2021-01-10', 'system');

-- ─────────────────────────────────────────────────────────────────
-- Hierarchies (Reporting Relationships)
-- ─────────────────────────────────────────────────────────────────
INSERT INTO hierarchies (employee_id, manager_id, business_unit_id, hierarchy_level, effective_from, is_active, assigned_by)
VALUES
    -- John reports to Jane at Paris
    ((SELECT id FROM users WHERE employee_number = 'EMP003'),
     (SELECT id FROM users WHERE employee_number = 'EMP002'),
     (SELECT id FROM business_units WHERE code = 'BU001'),
     1, '2020-03-01', true, 'system'),

    -- Jane reports to Admin at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP002'),
     (SELECT id FROM users WHERE employee_number = 'EMP001'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     1, '2020-01-15', true, 'system'),

    -- Bob reports to Admin at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP004'),
     (SELECT id FROM users WHERE employee_number = 'EMP001'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     1, '2020-02-01', true, 'system'),

    -- Alice reports to Admin at HQ
    ((SELECT id FROM users WHERE employee_number = 'EMP005'),
     (SELECT id FROM users WHERE employee_number = 'EMP001'),
     (SELECT id FROM business_units WHERE code = 'BU004'),
     1, '2021-01-10', true, 'system');

-- ─────────────────────────────────────────────────────────────────
-- Initial Activity Logs
-- ─────────────────────────────────────────────────────────────────
INSERT INTO user_activity_logs (user_id, activity_type, description, performed_at, performed_by)
VALUES
    ((SELECT id FROM users WHERE employee_number = 'EMP001'), 'CREATED', 'User account created', '2019-01-01 09:00:00', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP002'), 'CREATED', 'User account created', '2020-01-15 09:00:00', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP003'), 'CREATED', 'User account created', '2020-03-01 09:00:00', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP004'), 'CREATED', 'User account created', '2020-02-01 09:00:00', 'system'),
    ((SELECT id FROM users WHERE employee_number = 'EMP005'), 'CREATED', 'User account created', '2021-01-10 09:00:00', 'system');
