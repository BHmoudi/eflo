-- Test Data for User Service Integration Tests
-- This data is loaded after schema creation for testing purposes

-- Insert test business units
INSERT INTO business_units (id, code, name, type, description, is_active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'HQ', 'Headquarters', 'DEPARTMENT', 'Main headquarters', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'SALES', 'Sales Department', 'DEPARTMENT', 'Sales team', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'IT', 'IT Department', 'DEPARTMENT', 'Technology team', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', 'BRANCH1', 'Branch Office 1', 'BRANCH', 'First branch', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test users
INSERT INTO users (id, keycloak_id, email, first_name, last_name, phone_number, is_active, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'kc-user-1', 'john.doe@example.com', 'John', 'Doe', '+1234567890', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'kc-user-2', 'jane.smith@example.com', 'Jane', 'Smith', '+1234567891', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'kc-user-3', 'bob.jones@example.com', 'Bob', 'Jones', '+1234567892', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'kc-user-4', 'alice.brown@example.com', 'Alice', 'Brown', '+1234567893', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user-business unit assignments
INSERT INTO user_business_units (id, user_id, business_unit_id, role, is_primary, assigned_at)
VALUES
    ('e1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'ADMIN', true, CURRENT_TIMESTAMP),
    ('e2222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'MANAGER', true, CURRENT_TIMESTAMP),
    ('e3333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', 'AGENT', true, CURRENT_TIMESTAMP);

-- Insert hierarchies
INSERT INTO hierarchies (id, parent_business_unit_id, child_business_unit_id, hierarchy_level, created_at)
VALUES
    ('h1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 1, CURRENT_TIMESTAMP),
    ('h2222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 1, CURRENT_TIMESTAMP),
    ('h3333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444444', 2, CURRENT_TIMESTAMP);

-- Insert user roles
INSERT INTO user_roles (id, user_id, role_name, source, granted_at, granted_by)
VALUES
    ('r1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SYSTEM_ADMIN', 'MANUAL', CURRENT_TIMESTAMP, 'system'),
    ('r2222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MANAGER', 'KEYCLOAK', CURRENT_TIMESTAMP, 'keycloak-sync'),
    ('r3333333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'AGENT', 'KEYCLOAK', CURRENT_TIMESTAMP, 'keycloak-sync');

-- Insert user activity logs
INSERT INTO user_activity_logs (id, user_id, activity_type, description, ip_address, user_agent, created_at)
VALUES
    ('l1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'LOGIN', 'User logged in', '192.168.1.1', 'Mozilla/5.0', CURRENT_TIMESTAMP),
    ('l2222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'PROFILE_UPDATE', 'Updated profile information', '192.168.1.2', 'Mozilla/5.0', CURRENT_TIMESTAMP);
