-- ═══════════════════════════════════════════════════════════════════
-- V3: Create User-Business Unit Assignments Table
-- Description: Links users to business units with roles and permissions
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE user_business_units (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    business_unit_id BIGINT NOT NULL,

    -- Primary business unit flag
    is_primary BOOLEAN DEFAULT FALSE,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Assignment tracking
    assigned_at DATE,
    removed_at DATE,
    assigned_by VARCHAR(255),
    removed_by VARCHAR(255),

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_ubu_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ubu_business_unit FOREIGN KEY (business_unit_id)
        REFERENCES business_units(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_removed_date_after_assigned CHECK (removed_at IS NULL OR removed_at >= assigned_at)
);

-- Comments
COMMENT ON TABLE user_business_units IS 'User assignments to business units';
COMMENT ON COLUMN user_business_units.is_primary IS 'True if this is the users primary business unit';
COMMENT ON COLUMN user_business_units.assigned_by IS 'Username who assigned this user to business unit';
COMMENT ON COLUMN user_business_units.removed_by IS 'Username who removed this user from business unit';
