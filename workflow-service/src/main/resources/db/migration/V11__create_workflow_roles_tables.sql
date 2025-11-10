-- Migration V11: Create Workflow Roles and User Role Management Tables
-- This migration creates the foundation for role-based workflow management

-- ============================================
-- WORKFLOW ROLES
-- ============================================

-- Table: workflow_roles
-- Defines roles in the system (CDV, CDR, Admin, Commercial, etc.)
CREATE TABLE workflow_roles (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    description TEXT,
    level INTEGER NOT NULL, -- Hierarchy level (1=highest authority)
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_workflow_roles_code ON workflow_roles(role_code);
CREATE INDEX idx_workflow_roles_level ON workflow_roles(level);
CREATE INDEX idx_workflow_roles_active ON workflow_roles(is_active);

-- Table: workflow_user_roles
-- Maps users to roles with optional scope (affaire/branch)
CREATE TABLE workflow_user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL REFERENCES workflow_roles(id) ON DELETE CASCADE,
    affaire_code VARCHAR(100), -- Optional: role scoped to specific affaire
    branch_code VARCHAR(100), -- Optional: role scoped to specific branch
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_to TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_user_role_scope UNIQUE(user_id, role_id, affaire_code, branch_code)
);

CREATE INDEX idx_workflow_user_roles_user ON workflow_user_roles(user_id);
CREATE INDEX idx_workflow_user_roles_role ON workflow_user_roles(role_id);
CREATE INDEX idx_workflow_user_roles_affaire ON workflow_user_roles(affaire_code);
CREATE INDEX idx_workflow_user_roles_active ON workflow_user_roles(is_active);

-- Table: workflow_role_hierarchy
-- Defines role hierarchy for approval escalation and delegation
CREATE TABLE workflow_role_hierarchy (
    id BIGSERIAL PRIMARY KEY,
    parent_role_id BIGINT NOT NULL REFERENCES workflow_roles(id) ON DELETE CASCADE,
    child_role_id BIGINT NOT NULL REFERENCES workflow_roles(id) ON DELETE CASCADE,
    can_approve_for_child BOOLEAN DEFAULT true,
    can_delegate_to_child BOOLEAN DEFAULT false,
    can_view_child_tasks BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_role_hierarchy UNIQUE(parent_role_id, child_role_id),
    CONSTRAINT chk_no_self_hierarchy CHECK (parent_role_id != child_role_id)
);

CREATE INDEX idx_role_hierarchy_parent ON workflow_role_hierarchy(parent_role_id);
CREATE INDEX idx_role_hierarchy_child ON workflow_role_hierarchy(child_role_id);

-- ============================================
-- SEED DATA: Default Roles
-- ============================================

INSERT INTO workflow_roles (role_code, role_name, description, level, created_by) VALUES
('SUPER_ADMIN', 'Super Administrateur', 'Administrateur système avec tous les droits', 1, 'SYSTEM'),
('ADMIN', 'Administrateur', 'Administrateur de workflow', 2, 'SYSTEM'),
('CDR', 'Chef de Région (CDR)', 'Chef de Région - niveau validation le plus élevé', 3, 'SYSTEM'),
('CDV', 'Chef de Vente (CDV)', 'Chef de Vente - validation intermédiaire', 4, 'SYSTEM'),
('COMMERCIAL', 'Commercial', 'Vendeur commercial - créateur de commandes', 5, 'SYSTEM'),
('APPRO', 'Approvisionnement', 'Responsable approvisionnement', 4, 'SYSTEM'),
('FINANCE', 'Finance', 'Responsable financier', 4, 'SYSTEM'),
('PREPARATION', 'Préparation', 'Équipe de préparation véhicules', 5, 'SYSTEM'),
('LIVRAISON', 'Livraison', 'Équipe de livraison', 5, 'SYSTEM'),
('EXTERNE', 'Externe', 'Utilisateur externe (fournisseur, partenaire)', 6, 'SYSTEM');

-- Create role hierarchy
-- CDR > CDV > COMMERCIAL
INSERT INTO workflow_role_hierarchy (parent_role_id, child_role_id, can_approve_for_child, can_delegate_to_child)
SELECT
    (SELECT id FROM workflow_roles WHERE role_code = 'CDR'),
    (SELECT id FROM workflow_roles WHERE role_code = 'CDV'),
    true, true;

INSERT INTO workflow_role_hierarchy (parent_role_id, child_role_id, can_approve_for_child, can_delegate_to_child)
SELECT
    (SELECT id FROM workflow_roles WHERE role_code = 'CDV'),
    (SELECT id FROM workflow_roles WHERE role_code = 'COMMERCIAL'),
    true, true;

INSERT INTO workflow_role_hierarchy (parent_role_id, child_role_id, can_approve_for_child, can_delegate_to_child)
SELECT
    (SELECT id FROM workflow_roles WHERE role_code = 'CDR'),
    (SELECT id FROM workflow_roles WHERE role_code = 'COMMERCIAL'),
    true, false;

-- Admin hierarchy
INSERT INTO workflow_role_hierarchy (parent_role_id, child_role_id, can_approve_for_child, can_delegate_to_child)
SELECT
    (SELECT id FROM workflow_roles WHERE role_code = 'SUPER_ADMIN'),
    (SELECT id FROM workflow_roles WHERE role_code = 'ADMIN'),
    true, true;

-- Comments
COMMENT ON TABLE workflow_roles IS 'Defines user roles for workflow assignment and approvals';
COMMENT ON TABLE workflow_user_roles IS 'Maps users to roles with optional affaire/branch scope';
COMMENT ON TABLE workflow_role_hierarchy IS 'Defines role hierarchy for approval escalation';
COMMENT ON COLUMN workflow_roles.level IS 'Hierarchy level where 1 is highest authority';
COMMENT ON COLUMN workflow_user_roles.affaire_code IS 'Optional scope limiting role to specific affaire';
COMMENT ON COLUMN workflow_role_hierarchy.can_approve_for_child IS 'Parent role can approve tasks assigned to child role';
