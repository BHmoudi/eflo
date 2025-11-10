-- Document Types Table
-- Configurable document type definitions with validation rules

CREATE TABLE document_types (
    id BIGSERIAL PRIMARY KEY,
    type_code VARCHAR(100) NOT NULL UNIQUE,
    type_name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,

    -- Mandatory Settings
    is_mandatory BOOLEAN NOT NULL DEFAULT false,
    min_documents INTEGER DEFAULT 0,
    max_documents INTEGER DEFAULT 10,

    -- File Settings
    allowed_formats TEXT[] NOT NULL DEFAULT '{}',
    max_file_size_mb DECIMAL(10,2) NOT NULL DEFAULT 10.0,

    -- Validation Settings
    requires_validation BOOLEAN NOT NULL DEFAULT true,
    validator_roles TEXT[] DEFAULT '{}',
    auto_validate_conditions JSONB,

    -- Expiration Settings
    has_expiration BOOLEAN NOT NULL DEFAULT false,
    expiration_warning_days INTEGER DEFAULT 30,
    default_validity_days INTEGER,

    -- Business Rules
    custom_validation_rules JSONB,
    metadata_schema JSONB,

    -- Configuration
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER DEFAULT 0,

    -- Audit Fields
    business_unit_id BIGINT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,

    CONSTRAINT chk_min_max_documents CHECK (min_documents >= 0 AND max_documents >= min_documents),
    CONSTRAINT chk_max_file_size CHECK (max_file_size_mb > 0),
    CONSTRAINT chk_expiration_warning CHECK (expiration_warning_days >= 0)
);

-- Indexes
CREATE INDEX idx_document_types_category ON document_types(category);
CREATE INDEX idx_document_types_mandatory ON document_types(is_mandatory);
CREATE INDEX idx_document_types_business_unit ON document_types(business_unit_id);
CREATE INDEX idx_document_types_active ON document_types(is_active);

-- Comments
COMMENT ON TABLE document_types IS 'Configurable document type definitions with validation rules and constraints';
COMMENT ON COLUMN document_types.type_code IS 'Unique identifier code for the document type';
COMMENT ON COLUMN document_types.category IS 'Document category: IDENTITY, FINANCIAL, LEGAL, TECHNICAL, ADMINISTRATIVE, CONTRACT, OTHER';
COMMENT ON COLUMN document_types.auto_validate_conditions IS 'JSON configuration for automatic validation conditions';
COMMENT ON COLUMN document_types.custom_validation_rules IS 'JSON configuration for custom validation rules';
COMMENT ON COLUMN document_types.metadata_schema IS 'JSON schema defining required and optional metadata fields';
