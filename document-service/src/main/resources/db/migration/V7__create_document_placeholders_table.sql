-- Create document_placeholders table
-- This table tracks required documents for workflow instances

CREATE TABLE document_placeholders (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    workflow_instance_id BIGINT NOT NULL,
    document_type_id BIGINT NOT NULL,
    document_type_code VARCHAR(50) NOT NULL,
    document_type_name VARCHAR(255),
    is_mandatory BOOLEAN NOT NULL DEFAULT true,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_UPLOAD',
    uploaded_document_id BIGINT,
    deadline TIMESTAMP,
    validation_required BOOLEAN NOT NULL DEFAULT true,
    description TEXT,
    task_code VARCHAR(50),
    task_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_placeholder_document_type
        FOREIGN KEY (document_type_id)
        REFERENCES document_types(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_placeholder_uploaded_document
        FOREIGN KEY (uploaded_document_id)
        REFERENCES documents(id)
        ON DELETE SET NULL,

    CONSTRAINT unique_placeholder_per_order_workflow_type
        UNIQUE (order_id, workflow_instance_id, document_type_code)
);

-- Create indexes for performance
CREATE INDEX idx_doc_placeholders_order ON document_placeholders(order_id);
CREATE INDEX idx_doc_placeholders_instance ON document_placeholders(workflow_instance_id);
CREATE INDEX idx_doc_placeholders_type ON document_placeholders(document_type_code);
CREATE INDEX idx_doc_placeholders_status ON document_placeholders(status);
CREATE INDEX idx_doc_placeholders_mandatory ON document_placeholders(is_mandatory);
CREATE INDEX idx_doc_placeholders_deadline ON document_placeholders(deadline);

-- Add comments
COMMENT ON TABLE document_placeholders IS 'Tracks required documents for workflow instances';
COMMENT ON COLUMN document_placeholders.workflow_instance_id IS 'References workflow instance from workflow-service';
COMMENT ON COLUMN document_placeholders.uploaded_document_id IS 'Links to actual document when uploaded';
COMMENT ON COLUMN document_placeholders.deadline IS 'Upload deadline calculated from workflow state entry time';
