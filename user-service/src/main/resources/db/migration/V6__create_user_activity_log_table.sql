-- ═══════════════════════════════════════════════════════════════════
-- V6: Create User Activity Log Table
-- Description: Audit trail for user activities
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE user_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- Activity details
    activity_type VARCHAR(100) NOT NULL,
    description TEXT,

    -- Context
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),

    -- Business unit context
    business_unit_id BIGINT,
    business_unit_name VARCHAR(255),

    -- Additional metadata (JSON)
    metadata JSONB,

    -- Performed details
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    performed_by VARCHAR(255),

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign keys
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Comments
COMMENT ON TABLE user_activity_logs IS 'Audit trail of user activities';
COMMENT ON COLUMN user_activity_logs.activity_type IS 'Type of activity performed';
COMMENT ON COLUMN user_activity_logs.performed_by IS 'Username who performed this action';
COMMENT ON COLUMN user_activity_logs.business_unit_id IS 'Business unit context for this activity';
COMMENT ON COLUMN user_activity_logs.metadata IS 'Additional metadata in JSON format';
