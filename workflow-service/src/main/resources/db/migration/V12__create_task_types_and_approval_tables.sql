-- Migration V12: Create Task Types and Approval Chain Tables
-- This migration creates the infrastructure for configurable task types and multi-level approvals

-- ============================================
-- TASK TYPE CONFIGURATION
-- ============================================

-- Table: workflow_task_types
-- Defines task types (Document, Action, Control)
CREATE TABLE workflow_task_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(50) UNIQUE NOT NULL, -- DOCUMENT, ACTION, CONTROL
    type_name VARCHAR(255) NOT NULL,
    description TEXT,
    requires_upload BOOLEAN DEFAULT false, -- Documents need file uploads
    requires_user_input BOOLEAN DEFAULT false, -- Actions need data entry
    requires_approval BOOLEAN DEFAULT false, -- Controls need approval
    allows_comments BOOLEAN DEFAULT true,
    icon VARCHAR(50),
    color VARCHAR(20),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_types_code ON workflow_task_types(type_code);
CREATE INDEX idx_task_types_active ON workflow_task_types(is_active);

-- ============================================
-- APPROVAL CHAIN CONFIGURATION
-- ============================================

-- Table: workflow_approval_chains
-- Defines multi-level approval chains
CREATE TABLE workflow_approval_chains (
    id BIGSERIAL PRIMARY KEY,
    chain_code VARCHAR(50) UNIQUE NOT NULL,
    chain_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_approval_chains_code ON workflow_approval_chains(chain_code);
CREATE INDEX idx_approval_chains_active ON workflow_approval_chains(is_active);

-- Table: workflow_approval_levels
-- Defines levels within an approval chain
CREATE TABLE workflow_approval_levels (
    id BIGSERIAL PRIMARY KEY,
    chain_id BIGINT NOT NULL REFERENCES workflow_approval_chains(id) ON DELETE CASCADE,
    level_order INTEGER NOT NULL,
    level_name VARCHAR(255) NOT NULL,
    required_role_id BIGINT REFERENCES workflow_roles(id) ON DELETE RESTRICT,
    approval_type VARCHAR(20) DEFAULT 'SINGLE', -- SINGLE, ALL, MAJORITY
    is_parallel BOOLEAN DEFAULT false, -- Can approve in parallel with same level
    timeout_hours INTEGER,
    auto_approve_on_timeout BOOLEAN DEFAULT false,
    notify_on_assignment BOOLEAN DEFAULT true,
    notify_before_timeout_hours INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_chain_level UNIQUE(chain_id, level_order),
    CONSTRAINT chk_approval_type CHECK (approval_type IN ('SINGLE', 'ALL', 'MAJORITY'))
);

CREATE INDEX idx_approval_levels_chain ON workflow_approval_levels(chain_id);
CREATE INDEX idx_approval_levels_role ON workflow_approval_levels(required_role_id);
CREATE INDEX idx_approval_levels_order ON workflow_approval_levels(chain_id, level_order);

-- ============================================
-- TASK DEPENDENCIES AND CONDITIONS
-- ============================================

-- Table: workflow_task_dependencies
-- Defines dependencies between tasks
CREATE TABLE workflow_task_dependencies (
    id BIGSERIAL PRIMARY KEY,
    dependent_task_id BIGINT NOT NULL REFERENCES workflow_state_tasks(id) ON DELETE CASCADE,
    required_task_id BIGINT NOT NULL REFERENCES workflow_state_tasks(id) ON DELETE CASCADE,
    dependency_type VARCHAR(20) DEFAULT 'COMPLETION', -- COMPLETION, APPROVAL, STATUS
    required_status VARCHAR(50), -- Required status for dependency satisfaction
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_dependency UNIQUE(dependent_task_id, required_task_id),
    CONSTRAINT chk_no_self_dependency CHECK (dependent_task_id != required_task_id),
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('COMPLETION', 'APPROVAL', 'STATUS', 'FIELD_VALUE'))
);

CREATE INDEX idx_task_deps_dependent ON workflow_task_dependencies(dependent_task_id);
CREATE INDEX idx_task_deps_required ON workflow_task_dependencies(required_task_id);

-- Table: workflow_task_conditions
-- Defines conditions for conditional task activation
CREATE TABLE workflow_task_conditions (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES workflow_state_tasks(id) ON DELETE CASCADE,
    condition_type VARCHAR(50) NOT NULL, -- FIELD_VALUE, ROLE_BASED, DATE_BASED, CUSTOM
    condition_field VARCHAR(255), -- Field name to check
    condition_operator VARCHAR(20), -- EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, CONTAINS, etc.
    condition_value TEXT, -- Expected value
    condition_expression TEXT, -- For complex conditions (JSON or SpEL expression)
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_condition_type CHECK (condition_type IN ('FIELD_VALUE', 'ROLE_BASED', 'DATE_BASED', 'CUSTOM', 'ORDER_CRITERIA'))
);

CREATE INDEX idx_task_conditions_task ON workflow_task_conditions(task_id);
CREATE INDEX idx_task_conditions_active ON workflow_task_conditions(is_active);

-- ============================================
-- ENHANCE EXISTING TABLES
-- ============================================

-- Add new columns to workflow_state_tasks
ALTER TABLE workflow_state_tasks
    ADD COLUMN IF NOT EXISTS assigned_role_id BIGINT REFERENCES workflow_roles(id),
    ADD COLUMN IF NOT EXISTS task_type_id BIGINT REFERENCES workflow_task_types(id),
    ADD COLUMN IF NOT EXISTS required_documents JSONB, -- List of required document types
    ADD COLUMN IF NOT EXISTS input_fields JSONB, -- Dynamic form fields for actions
    ADD COLUMN IF NOT EXISTS validation_rules JSONB, -- Validation rules for inputs
    ADD COLUMN IF NOT EXISTS notification_template TEXT, -- Email/SMS template
    ADD COLUMN IF NOT EXISTS sla_hours INTEGER, -- Service Level Agreement in hours
    ADD COLUMN IF NOT EXISTS escalation_hours INTEGER, -- Escalation timeout
    ADD COLUMN IF NOT EXISTS allow_delegation BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS visible_to_roles BIGINT[], -- Array of role IDs who can view this task
    ADD COLUMN IF NOT EXISTS editable_after_completion BOOLEAN DEFAULT false;

-- Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_state_tasks_role ON workflow_state_tasks(assigned_role_id);
CREATE INDEX IF NOT EXISTS idx_state_tasks_type ON workflow_state_tasks(task_type_id);

-- Table to link tasks with approval chains
CREATE TABLE workflow_task_approval_chains (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES workflow_state_tasks(id) ON DELETE CASCADE,
    chain_id BIGINT NOT NULL REFERENCES workflow_approval_chains(id) ON DELETE RESTRICT,
    is_mandatory BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_chain UNIQUE(task_id, chain_id)
);

CREATE INDEX idx_task_chains_task ON workflow_task_approval_chains(task_id);
CREATE INDEX idx_task_chains_chain ON workflow_task_approval_chains(chain_id);

-- ============================================
-- SEED DATA: Task Types
-- ============================================

INSERT INTO workflow_task_types (type_code, type_name, description, requires_upload, requires_user_input, requires_approval, icon, color, display_order) VALUES
('DOCUMENT', 'Document', 'Task requiring document upload', true, false, false, 'file-text', '#3B82F6', 1),
('ACTION', 'Action', 'Task requiring user data input or action', false, true, false, 'check-square', '#10B981', 2),
('CONTROL', 'Contrôle', 'Task requiring validation or approval', false, false, true, 'shield-check', '#F59E0B', 3);

-- ============================================
-- SEED DATA: Approval Chains
-- ============================================

-- CDV Approval Chain (Single level)
INSERT INTO workflow_approval_chains (chain_code, chain_name, description, created_by) VALUES
('CDV_APPROVAL', 'Validation CDV', 'Approbation par le Chef de Vente uniquement', 'SYSTEM');

INSERT INTO workflow_approval_levels (chain_id, level_order, level_name, required_role_id, approval_type, timeout_hours, notify_before_timeout_hours)
SELECT
    (SELECT id FROM workflow_approval_chains WHERE chain_code = 'CDV_APPROVAL'),
    1,
    'Validation par CDV',
    (SELECT id FROM workflow_roles WHERE role_code = 'CDV'),
    'SINGLE',
    48,
    12;

-- CDV then CDR Approval Chain (Two levels)
INSERT INTO workflow_approval_chains (chain_code, chain_name, description, created_by) VALUES
('CDV_CDR_CHAIN', 'Validation CDV puis CDR', 'Approbation séquentielle: CDV puis CDR', 'SYSTEM');

INSERT INTO workflow_approval_levels (chain_id, level_order, level_name, required_role_id, approval_type, timeout_hours, notify_before_timeout_hours)
SELECT
    (SELECT id FROM workflow_approval_chains WHERE chain_code = 'CDV_CDR_CHAIN'),
    1,
    'Validation par CDV',
    (SELECT id FROM workflow_roles WHERE role_code = 'CDV'),
    'SINGLE',
    48,
    12;

INSERT INTO workflow_approval_levels (chain_id, level_order, level_name, required_role_id, approval_type, timeout_hours, notify_before_timeout_hours)
SELECT
    (SELECT id FROM workflow_approval_chains WHERE chain_code = 'CDV_CDR_CHAIN'),
    2,
    'Validation par CDR',
    (SELECT id FROM workflow_roles WHERE role_code = 'CDR'),
    'SINGLE',
    48,
    12;

-- Admin Approval Chain
INSERT INTO workflow_approval_chains (chain_code, chain_name, description, created_by) VALUES
('ADMIN_APPROVAL', 'Validation Administrateur', 'Approbation par administrateur', 'SYSTEM');

INSERT INTO workflow_approval_levels (chain_id, level_order, level_name, required_role_id, approval_type, timeout_hours)
SELECT
    (SELECT id FROM workflow_approval_chains WHERE chain_code = 'ADMIN_APPROVAL'),
    1,
    'Validation Administrateur',
    (SELECT id FROM workflow_roles WHERE role_code = 'ADMIN'),
    'SINGLE',
    24;

-- Comments
COMMENT ON TABLE workflow_task_types IS 'Defines configurable task types (Document, Action, Control)';
COMMENT ON TABLE workflow_approval_chains IS 'Defines multi-level approval workflows';
COMMENT ON TABLE workflow_approval_levels IS 'Individual levels within an approval chain with role requirements';
COMMENT ON TABLE workflow_task_dependencies IS 'Defines task dependencies for sequential execution';
COMMENT ON TABLE workflow_task_conditions IS 'Conditional logic for dynamic task activation';
COMMENT ON COLUMN workflow_approval_levels.approval_type IS 'SINGLE: one approver, ALL: all with role, MAJORITY: >50%';
COMMENT ON COLUMN workflow_state_tasks.input_fields IS 'JSON schema defining dynamic form fields for action tasks';
COMMENT ON COLUMN workflow_state_tasks.validation_rules IS 'JSON validation rules for task inputs';
