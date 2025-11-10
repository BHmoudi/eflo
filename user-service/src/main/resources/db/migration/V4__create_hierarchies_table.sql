-- ═══════════════════════════════════════════════════════════════════
-- V4: Create Hierarchies Table
-- Description: Reporting relationships and organizational structure
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE hierarchies (
    id BIGSERIAL PRIMARY KEY,

    -- Relationship
    employee_id BIGINT NOT NULL,
    manager_id BIGINT NOT NULL,

    -- Context
    business_unit_id BIGINT,

    -- Hierarchy level (1=direct report, 2=skip-level, etc.)
    hierarchy_level INT DEFAULT 1,

    -- Period
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Assignment tracking
    assigned_by VARCHAR(255),
    removed_by VARCHAR(255),

    -- Notes
    notes TEXT,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_hierarchy_employee FOREIGN KEY (employee_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_hierarchy_manager FOREIGN KEY (manager_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_hierarchy_business_unit FOREIGN KEY (business_unit_id)
        REFERENCES business_units(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_no_self_reporting CHECK (employee_id != manager_id),
    CONSTRAINT uk_hierarchy_active UNIQUE (employee_id, business_unit_id, effective_from),
    CONSTRAINT chk_hierarchy_level CHECK (hierarchy_level >= 1),
    CONSTRAINT chk_hierarchy_end_date CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

-- Comments
COMMENT ON TABLE hierarchies IS 'Organizational reporting relationships';
COMMENT ON COLUMN hierarchies.hierarchy_level IS 'Hierarchy level: 1=direct report, 2=skip-level, etc.';
COMMENT ON COLUMN hierarchies.business_unit_id IS 'Context for this reporting relationship';
COMMENT ON COLUMN hierarchies.assigned_by IS 'Username who assigned this hierarchy';
COMMENT ON COLUMN hierarchies.removed_by IS 'Username who removed this hierarchy';
