-- Create workflow_processes table
-- Stores workflow process definitions (templates)

CREATE TABLE workflow_processes (
    id BIGSERIAL PRIMARY KEY,
    process_code VARCHAR(50) NOT NULL UNIQUE,
    process_name VARCHAR(255) NOT NULL,
    description TEXT,
    order_type VARCHAR(20) NOT NULL,
    process_version INTEGER NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true,
    max_duration_days INTEGER,
    auto_progress_enabled BOOLEAN NOT NULL DEFAULT false,
    parallel_execution_allowed BOOLEAN NOT NULL DEFAULT false,
    configuration JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT check_order_type CHECK (order_type IN ('VN', 'VO', 'EVO'))
);

-- Create indexes
CREATE INDEX idx_workflow_processes_code ON workflow_processes(process_code);
CREATE INDEX idx_workflow_processes_order_type ON workflow_processes(order_type);
CREATE INDEX idx_workflow_processes_active ON workflow_processes(is_active);
CREATE INDEX idx_workflow_processes_config ON workflow_processes USING gin(configuration);

-- Add comments
COMMENT ON TABLE workflow_processes IS 'Stores workflow process definitions (templates)';
COMMENT ON COLUMN workflow_processes.process_code IS 'Unique code identifier for the process (e.g., VN_STANDARD)';
COMMENT ON COLUMN workflow_processes.order_type IS 'Type of order this workflow applies to (VN, VO, EVO)';
COMMENT ON COLUMN workflow_processes.process_version IS 'Version number for workflow versioning';
COMMENT ON COLUMN workflow_processes.configuration IS 'Additional JSON configuration for the process';
