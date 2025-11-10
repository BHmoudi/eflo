-- Document Access Log Table
-- Comprehensive audit trail for all document access and operations

CREATE TYPE access_action AS ENUM (
    'UPLOAD',
    'DOWNLOAD',
    'VIEW',
    'UPDATE',
    'DELETE',
    'VALIDATE',
    'REJECT',
    'ARCHIVE',
    'RESTORE',
    'SHARE',
    'EXPORT'
);

CREATE TABLE document_access_log (
    id BIGSERIAL PRIMARY KEY,
    log_uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),

    -- Document Reference
    document_id BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    document_uuid UUID,
    document_filename VARCHAR(500),

    -- Access Details
    action access_action NOT NULL,
    action_description TEXT,
    action_result VARCHAR(50) NOT NULL, -- SUCCESS, FAILURE, PARTIAL

    -- User Context
    user_id VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    user_role VARCHAR(100),
    business_unit_id BIGINT,

    -- Technical Context
    ip_address VARCHAR(45), -- IPv4 or IPv6
    user_agent TEXT,
    session_id VARCHAR(255),

    -- Request Details
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    response_status INTEGER,

    -- Metadata
    metadata JSONB,
    error_message TEXT,

    -- Timestamp
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Data Retention
    retention_until DATE
);

-- Indexes
CREATE INDEX idx_access_log_document ON document_access_log(document_id);
CREATE INDEX idx_access_log_document_uuid ON document_access_log(document_uuid);
CREATE INDEX idx_access_log_user ON document_access_log(user_id);
CREATE INDEX idx_access_log_action ON document_access_log(action);
CREATE INDEX idx_access_log_accessed_at ON document_access_log(accessed_at DESC);
CREATE INDEX idx_access_log_business_unit ON document_access_log(business_unit_id);
CREATE INDEX idx_access_log_retention ON document_access_log(retention_until) WHERE retention_until IS NOT NULL;

-- Composite Indexes
CREATE INDEX idx_access_log_doc_user ON document_access_log(document_id, user_id);
CREATE INDEX idx_access_log_user_action ON document_access_log(user_id, action, accessed_at DESC);

-- GIN Index for JSONB
CREATE INDEX idx_access_log_metadata ON document_access_log USING GIN (metadata);

-- Partitioning preparation (optional - for high-volume environments)
-- This can be enabled later for time-based partitioning

-- Comments
COMMENT ON TABLE document_access_log IS 'Comprehensive audit trail for all document access and operations';
COMMENT ON COLUMN document_access_log.action_result IS 'Result of the action: SUCCESS, FAILURE, PARTIAL';
COMMENT ON COLUMN document_access_log.metadata IS 'Additional context data in JSON format';
COMMENT ON COLUMN document_access_log.retention_until IS 'Date until which this log entry should be retained';
