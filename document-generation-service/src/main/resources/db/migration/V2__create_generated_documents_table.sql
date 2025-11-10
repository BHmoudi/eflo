-- Create generation_status enum
CREATE TYPE generation_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED');

-- Create generated_documents table
CREATE TABLE generated_documents (
    id BIGSERIAL PRIMARY KEY,
    document_uuid VARCHAR(255) UNIQUE NOT NULL,
    template_id BIGINT NOT NULL REFERENCES document_templates(id) ON DELETE RESTRICT,
    order_id BIGINT NOT NULL,
    workflow_instance_id BIGINT,
    generated_content BYTEA,
    file_size_bytes BIGINT,
    filename VARCHAR(255),
    mime_type VARCHAR(100),
    variables_used JSONB,
    generation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    uploaded_to_document_service BOOLEAN DEFAULT false,
    document_service_id BIGINT,
    requires_approval BOOLEAN DEFAULT false,
    approval_roles VARCHAR(255),
    is_approved BOOLEAN DEFAULT false,
    approved_by VARCHAR(100),
    approved_by_role VARCHAR(100),
    approval_date TIMESTAMP,
    approval_comments TEXT,
    generation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generated_by VARCHAR(100)
);

CREATE INDEX idx_generated_order ON generated_documents(order_id);
CREATE INDEX idx_generated_workflow ON generated_documents(workflow_instance_id);
CREATE INDEX idx_generated_template ON generated_documents(template_id);
CREATE INDEX idx_generated_status ON generated_documents(generation_status);
CREATE INDEX idx_generated_uuid ON generated_documents(document_uuid);
CREATE INDEX idx_generated_approval_pending ON generated_documents(requires_approval, is_approved);

COMMENT ON TABLE generated_documents IS 'Documents generated from templates';
COMMENT ON COLUMN generated_documents.variables_used IS 'Actual variable values used during generation';
COMMENT ON COLUMN generated_documents.approval_roles IS 'Roles allowed to approve this specific document';
