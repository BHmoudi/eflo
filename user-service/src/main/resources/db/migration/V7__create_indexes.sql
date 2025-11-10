-- ═══════════════════════════════════════════════════════════════════
-- V7: Create Indexes for Performance Optimization
-- Description: Indexes for all tables to improve query performance
-- ═══════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────
-- Users Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_employee_number ON users(employee_number);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_full_name ON users(full_name);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_users_department ON users(department) WHERE department IS NOT NULL;
CREATE INDEX idx_users_last_login ON users(last_login_at);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ─────────────────────────────────────────────────────────────────
-- Business Units Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_business_units_code ON business_units(code);
CREATE INDEX idx_business_units_region ON business_units(region_code);
CREATE INDEX idx_business_units_type ON business_units(type);
CREATE INDEX idx_business_units_active ON business_units(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_business_units_manager ON business_units(manager_id);
CREATE INDEX idx_business_units_city ON business_units(city);

-- ─────────────────────────────────────────────────────────────────
-- User-Business Units Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_ubu_user ON user_business_units(user_id);
CREATE INDEX idx_ubu_business_unit ON user_business_units(business_unit_id);
CREATE INDEX idx_ubu_dates ON user_business_units(assigned_at, removed_at);
CREATE INDEX idx_ubu_primary ON user_business_units(user_id, is_primary) WHERE is_primary = TRUE;
CREATE INDEX idx_ubu_active ON user_business_units(is_active) WHERE is_active = TRUE;

-- ─────────────────────────────────────────────────────────────────
-- Hierarchies Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_hierarchies_employee ON hierarchies(employee_id);
CREATE INDEX idx_hierarchies_manager ON hierarchies(manager_id);
CREATE INDEX idx_hierarchies_business_unit ON hierarchies(business_unit_id);
CREATE INDEX idx_hierarchies_active ON hierarchies(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_hierarchies_dates ON hierarchies(effective_from, effective_to);
CREATE INDEX idx_hierarchies_level ON hierarchies(hierarchy_level);

-- ─────────────────────────────────────────────────────────────────
-- User Roles Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_name);
CREATE INDEX idx_user_roles_business_unit ON user_roles(business_unit_id);
CREATE INDEX idx_user_roles_active ON user_roles(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_user_roles_source ON user_roles(source);

-- ─────────────────────────────────────────────────────────────────
-- User Activity Log Table Indexes
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_activity_log_user ON user_activity_logs(user_id);
CREATE INDEX idx_activity_log_type ON user_activity_logs(activity_type);
CREATE INDEX idx_activity_log_timestamp ON user_activity_logs(performed_at);
CREATE INDEX idx_activity_log_performed_by ON user_activity_logs(performed_by);
CREATE INDEX idx_activity_log_business_unit ON user_activity_logs(business_unit_id);

-- ─────────────────────────────────────────────────────────────────
-- Composite Indexes for Common Queries
-- ─────────────────────────────────────────────────────────────────
CREATE INDEX idx_users_active_department ON users(is_active, department) WHERE is_active = TRUE;
CREATE INDEX idx_ubu_user_active ON user_business_units(user_id, is_active) WHERE is_active = TRUE;
CREATE INDEX idx_hierarchies_manager_active ON hierarchies(manager_id, is_active) WHERE is_active = TRUE;
