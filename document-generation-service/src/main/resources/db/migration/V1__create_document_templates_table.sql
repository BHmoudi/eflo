-- Create document_templates table

CREATE TABLE document_templates (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(50) UNIQUE NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    description TEXT,
    template_format VARCHAR(20) NOT NULL,
    template_content BYTEA,
    template_size_bytes BIGINT,
    original_filename VARCHAR(255),
    variables JSONB,
    generation_config JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    version INT NOT NULL DEFAULT 1,
    language VARCHAR(5) DEFAULT 'FR',
    applicable_order_types VARCHAR(100),
    auto_generate_on_states JSONB,
    requires_approval BOOLEAN NOT NULL DEFAULT false,
    approval_roles VARCHAR(255),
    blocks_workflow_progression BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_template_code ON document_templates(template_code);
CREATE INDEX idx_template_type ON document_templates(template_type);
CREATE INDEX idx_template_active ON document_templates(is_active);
CREATE INDEX idx_template_language ON document_templates(language);

COMMENT ON TABLE document_templates IS 'Document templates for auto-generation';
COMMENT ON COLUMN document_templates.variables IS 'JSON array of placeholder variables with metadata';
COMMENT ON COLUMN document_templates.auto_generate_on_states IS 'Workflow states that trigger auto-generation';
COMMENT ON COLUMN document_templates.approval_roles IS 'Comma-separated list of roles that can approve generated documents';
