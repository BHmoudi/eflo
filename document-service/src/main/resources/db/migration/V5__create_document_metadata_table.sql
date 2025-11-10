-- Document Metadata Table
-- Extended metadata storage for documents with flexible schema

CREATE TABLE document_metadata (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,

    -- Metadata Key-Value
    metadata_key VARCHAR(100) NOT NULL,
    metadata_value TEXT,
    value_type VARCHAR(50) NOT NULL DEFAULT 'STRING', -- STRING, NUMBER, DATE, BOOLEAN, JSON

    -- Metadata Classification
    is_searchable BOOLEAN DEFAULT true,
    is_encrypted BOOLEAN DEFAULT false,
    is_required BOOLEAN DEFAULT false,

    -- Validation
    validation_regex VARCHAR(500),
    allowed_values TEXT[],

    -- Audit
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,

    CONSTRAINT uq_document_metadata_key UNIQUE (document_id, metadata_key)
);

-- Indexes
CREATE INDEX idx_metadata_document ON document_metadata(document_id);
CREATE INDEX idx_metadata_key ON document_metadata(metadata_key);
CREATE INDEX idx_metadata_searchable ON document_metadata(is_searchable) WHERE is_searchable = true;
CREATE INDEX idx_metadata_value ON document_metadata(metadata_value) WHERE is_searchable = true;

-- Composite Indexes
CREATE INDEX idx_metadata_key_value ON document_metadata(metadata_key, metadata_value) WHERE is_searchable = true;

-- Comments
COMMENT ON TABLE document_metadata IS 'Extended metadata storage for documents with flexible schema';
COMMENT ON COLUMN document_metadata.value_type IS 'Data type of the metadata value: STRING, NUMBER, DATE, BOOLEAN, JSON';
COMMENT ON COLUMN document_metadata.is_encrypted IS 'Indicates if the value is encrypted';
