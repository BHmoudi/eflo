-- ═══════════════════════════════════════════════════════════════════
-- V5: Create User Roles Table
-- Description: Role assignments synchronized from Keycloak
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- Role information
    role_name VARCHAR(50) NOT NULL,

    -- Context (optional business unit scope)
    business_unit_id BIGINT,

    -- Source
    source VARCHAR(20) DEFAULT 'MANUAL' CHECK (source IN ('KEYCLOAK', 'MANUAL')),

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Assignment tracking
    assigned_at DATE,
    revoked_at DATE,
    assigned_by VARCHAR(255),
    revoked_by VARCHAR(255),

    -- Notes
    notes TEXT,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_from_keycloak_at TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_business_unit FOREIGN KEY (business_unit_id)
        REFERENCES business_units(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT uk_user_role_active UNIQUE (user_id, role_name, business_unit_id),
    CONSTRAINT chk_role_name CHECK (role_name IN (
        'SUPER_ADMIN',
        'ADMIN_LOCAL',
        'SALES_MANAGER',
        'DOCUMENT_MANAGER',
        'SALESPERSON',
        'ACCOUNTANT',
        'SECRETARY',
        'VIEWER',
        'EXTERNAL_API_READ',
        'EXTERNAL_API_WRITE'
    ))
);

-- Comments
COMMENT ON TABLE user_roles IS 'User role assignments from Keycloak';
COMMENT ON COLUMN user_roles.role_name IS 'Keycloak role name (must match Eflo realm roles)';
COMMENT ON COLUMN user_roles.source IS 'Source of role assignment: KEYCLOAK or MANUAL';
COMMENT ON COLUMN user_roles.business_unit_id IS 'Optional business unit scope for role';
COMMENT ON COLUMN user_roles.assigned_by IS 'Username who assigned this role';
COMMENT ON COLUMN user_roles.revoked_by IS 'Username who revoked this role';
COMMENT ON COLUMN user_roles.synced_from_keycloak_at IS 'Timestamp when role was last synced from Keycloak';
