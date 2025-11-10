-- Create workflow_state_task_documents table
-- This table links tasks to document type requirements

CREATE TABLE workflow_state_task_documents (
    id BIGSERIAL PRIMARY KEY,
    state_task_id BIGINT NOT NULL,
    document_type_code VARCHAR(50) NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT true,
    upload_deadline_hours INT,
    validation_required BOOLEAN NOT NULL DEFAULT true,
    display_order INT NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_doc_state_task
        FOREIGN KEY (state_task_id)
        REFERENCES workflow_state_tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT unique_doc_per_task
        UNIQUE (state_task_id, document_type_code)
);

-- Create indexes for performance
CREATE INDEX idx_task_docs_state_task ON workflow_state_task_documents(state_task_id);
CREATE INDEX idx_task_docs_type_code ON workflow_state_task_documents(document_type_code);
CREATE INDEX idx_task_docs_mandatory ON workflow_state_task_documents(is_mandatory);

-- Add comment
COMMENT ON TABLE workflow_state_task_documents IS 'Document requirements for workflow state tasks';
COMMENT ON COLUMN workflow_state_task_documents.document_type_code IS 'References document type code from document-service';
COMMENT ON COLUMN workflow_state_task_documents.upload_deadline_hours IS 'Deadline in hours from state entry';
