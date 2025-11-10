-- Documents Table
-- Main table for document storage and management with versioning support

CREATE TYPE document_status AS ENUM (
    'PENDING',
    'VALIDATED',
    'REJECTED',
    'EXPIRED',
    'ARCHIVED',
    'DELETED'
);

CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    document_uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),

    -- Document Type Reference
    document_type_id BIGINT NOT NULL REFERENCES document_types(id),
    type_code VARCHAR(100) NOT NULL,

    -- Associated Order
    order_id BIGINT NOT NULL,
    order_number VARCHAR(100) NOT NULL,

    -- File Information
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(500) NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    file_hash VARCHAR(128), -- SHA-256 hash for duplicate detection

    -- Storage Information
    storage_bucket VARCHAR(100) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    storage_region VARCHAR(50),

    -- Version Control
    version INTEGER NOT NULL DEFAULT 1,
    is_latest_version BOOLEAN NOT NULL DEFAULT true,
    parent_document_id BIGINT REFERENCES documents(id),
    replaced_by_document_id BIGINT REFERENCES documents(id),

    -- Document Status
    status document_status NOT NULL DEFAULT 'PENDING',
    status_reason TEXT,

    -- Validation
    validated_at TIMESTAMP,
    validated_by VARCHAR(255),
    validation_comments TEXT,

    -- Expiration
    expiration_date DATE,
    expiration_notified BOOLEAN DEFAULT false,
    expiration_notification_sent_at TIMESTAMP,

    -- Virus Scanning
    virus_scan_status VARCHAR(50), -- PENDING, CLEAN, INFECTED, FAILED, SKIPPED
    virus_scan_date TIMESTAMP,
    virus_scan_result JSONB,

    -- Metadata
    custom_metadata JSONB,
    tags TEXT[] DEFAULT '{}',
    description TEXT,

    -- Security
    is_confidential BOOLEAN DEFAULT false,
    access_level VARCHAR(50) DEFAULT 'STANDARD', -- STANDARD, RESTRICTED, CONFIDENTIAL
    encryption_enabled BOOLEAN DEFAULT false,

    -- Business Context
    business_unit_id BIGINT,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),

    CONSTRAINT chk_file_size CHECK (file_size_bytes > 0),
    CONSTRAINT chk_version CHECK (version > 0)
);

-- Indexes for Performance
CREATE INDEX idx_documents_uuid ON documents(document_uuid);
CREATE INDEX idx_documents_type ON documents(document_type_id);
CREATE INDEX idx_documents_order ON documents(order_id);
CREATE INDEX idx_documents_order_number ON documents(order_number);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at DESC);
CREATE INDEX idx_documents_expiration ON documents(expiration_date) WHERE expiration_date IS NOT NULL;
CREATE INDEX idx_documents_business_unit ON documents(business_unit_id);
CREATE INDEX idx_documents_latest_version ON documents(is_latest_version) WHERE is_latest_version = true;
CREATE INDEX idx_documents_file_hash ON documents(file_hash);
CREATE INDEX idx_documents_virus_scan ON documents(virus_scan_status) WHERE virus_scan_status = 'PENDING';
CREATE INDEX idx_documents_deleted ON documents(deleted_at) WHERE deleted_at IS NULL;

-- Composite Indexes
CREATE INDEX idx_documents_order_type ON documents(order_id, document_type_id);
CREATE INDEX idx_documents_order_status ON documents(order_id, status);
CREATE INDEX idx_documents_type_status ON documents(document_type_id, status);

-- GIN Indexes for JSONB and Array
CREATE INDEX idx_documents_custom_metadata ON documents USING GIN (custom_metadata);
CREATE INDEX idx_documents_tags ON documents USING GIN (tags);

-- Comments
COMMENT ON TABLE documents IS 'Main document storage table with versioning and comprehensive metadata';
COMMENT ON COLUMN documents.document_uuid IS 'Globally unique identifier for the document';
COMMENT ON COLUMN documents.file_hash IS 'SHA-256 hash for duplicate detection and integrity verification';
COMMENT ON COLUMN documents.version IS 'Version number for document versioning';
COMMENT ON COLUMN documents.is_latest_version IS 'Flag indicating if this is the current version';
COMMENT ON COLUMN documents.parent_document_id IS 'Reference to the original document when versioning';
COMMENT ON COLUMN documents.custom_metadata IS 'Flexible JSON storage for document-specific metadata';
