-- Document Validation Rules Table
-- Custom validation rules engine for flexible document validation

CREATE TYPE validation_rule_type AS ENUM (
    'FILE_SIZE',
    'FILE_FORMAT',
    'FILE_NAME_PATTERN',
    'CONTENT_CHECK',
    'METADATA_REQUIRED',
    'BUSINESS_RULE',
    'CUSTOM_SCRIPT'
);

CREATE TABLE document_validation_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(100) NOT NULL UNIQUE,
    rule_name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Rule Configuration
    rule_type validation_rule_type NOT NULL,
    document_type_id BIGINT REFERENCES document_types(id) ON DELETE CASCADE,
    applies_to_all_types BOOLEAN DEFAULT false,

    -- Rule Definition
    rule_configuration JSONB NOT NULL,
    validation_script TEXT,

    -- Rule Behavior
    is_blocking BOOLEAN NOT NULL DEFAULT true,
    execution_order INTEGER DEFAULT 0,
    error_message_template VARCHAR(500),

    -- Conditions
    condition_expression TEXT,
    enabled_when JSONB,

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Audit
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,

    CONSTRAINT chk_execution_order CHECK (execution_order >= 0)
);

-- Indexes
CREATE INDEX idx_validation_rules_type ON document_validation_rules(rule_type);
CREATE INDEX idx_validation_rules_document_type ON document_validation_rules(document_type_id);
CREATE INDEX idx_validation_rules_active ON document_validation_rules(is_active);
CREATE INDEX idx_validation_rules_execution_order ON document_validation_rules(execution_order);

-- GIN Index for JSONB
CREATE INDEX idx_validation_rules_config ON document_validation_rules USING GIN (rule_configuration);

-- Comments
COMMENT ON TABLE document_validation_rules IS 'Custom validation rules for flexible document validation';
COMMENT ON COLUMN document_validation_rules.rule_configuration IS 'JSON configuration for rule parameters';
COMMENT ON COLUMN document_validation_rules.is_blocking IS 'If true, validation failure prevents document acceptance';
COMMENT ON COLUMN document_validation_rules.execution_order IS 'Order in which rules are executed (lower first)';
