-- Create workflow_states table
-- Stores state definitions for each workflow process

CREATE TABLE workflow_states (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL,
    state_code VARCHAR(50) NOT NULL,
    state_name VARCHAR(255) NOT NULL,
    description TEXT,
    state_order INTEGER NOT NULL,
    state_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    expected_duration_hours INTEGER,
    is_final_state BOOLEAN NOT NULL DEFAULT false,
    requires_approval BOOLEAN NOT NULL DEFAULT false,
    allow_skip BOOLEAN NOT NULL DEFAULT false,
    configuration JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_states_process FOREIGN KEY (process_id)
        REFERENCES workflow_processes(id) ON DELETE CASCADE,
    CONSTRAINT check_state_type CHECK (state_type IN ('START', 'NORMAL', 'DECISION', 'FINAL', 'ERROR')),
    CONSTRAINT unique_state_code_per_process UNIQUE (process_id, state_code)
);

-- Create indexes
CREATE INDEX idx_workflow_states_process ON workflow_states(process_id);
CREATE INDEX idx_workflow_states_code ON workflow_states(state_code);
CREATE INDEX idx_workflow_states_type ON workflow_states(state_type);
CREATE INDEX idx_workflow_states_order ON workflow_states(process_id, state_order);
CREATE INDEX idx_workflow_states_config ON workflow_states USING gin(configuration);

-- Add comments
COMMENT ON TABLE workflow_states IS 'Stores state definitions for each workflow process';
COMMENT ON COLUMN workflow_states.state_code IS 'Unique code for the state within the process';
COMMENT ON COLUMN workflow_states.state_order IS 'Display order of the state in the workflow';
COMMENT ON COLUMN workflow_states.state_type IS 'Type of state (START, NORMAL, DECISION, FINAL, ERROR)';
COMMENT ON COLUMN workflow_states.expected_duration_hours IS 'Expected time to complete this state';
